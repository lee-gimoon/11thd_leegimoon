package io.e2d.projectcollab.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StageFiveTaskConcurrencyIntegrationTest {

    private static final String REQUESTER_ID = "X-Requester-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rejectsStaleVersionAndKeepsTheFirstUpdate() throws Exception {
        TestFixture fixture = createFixture("stale");

        mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}",
                            fixture.projectId(), fixture.taskId())
                        .header(REQUESTER_ID, fixture.ownerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskUpdateBody(
                                "A가 수정한 작업",
                                "IN_PROGRESS",
                                fixture.memberId(),
                                fixture.version()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("A가 수정한 작업"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}",
                            fixture.projectId(), fixture.taskId())
                        .header(REQUESTER_ID, fixture.memberId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskUpdateBody(
                                "B가 수정한 작업",
                                "DONE",
                                fixture.memberId(),
                                fixture.version()
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TASK_VERSION_CONFLICT"));

        mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}",
                            fixture.projectId(), fixture.taskId())
                        .header(REQUESTER_ID, fixture.ownerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("A가 수정한 작업"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void onlyOneOfTwoConcurrentUpdatesSucceeds() throws Exception {
        TestFixture fixture = createFixture("concurrent");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<UpdateResult> first = executor.submit(() -> performConcurrentUpdate(
                    fixture,
                    fixture.ownerId(),
                    "동시 수정 A",
                    "IN_PROGRESS",
                    ready,
                    start
            ));
            Future<UpdateResult> second = executor.submit(() -> performConcurrentUpdate(
                    fixture,
                    fixture.memberId(),
                    "동시 수정 B",
                    "DONE",
                    ready,
                    start
            ));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<UpdateResult> results = List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS)
            );
            assertThat(results)
                    .extracting(UpdateResult::status)
                    .containsExactlyInAnyOrder(200, 409);

            UpdateResult success = results.stream()
                    .filter(result -> result.status() == 200)
                    .findFirst()
                    .orElseThrow();
            UpdateResult conflict = results.stream()
                    .filter(result -> result.status() == 409)
                    .findFirst()
                    .orElseThrow();

            JsonNode successBody = objectMapper.readTree(success.body());
            JsonNode conflictBody = objectMapper.readTree(conflict.body());
            assertThat(successBody.get("version").asLong()).isEqualTo(1);
            assertThat(conflictBody.get("code").asText()).isEqualTo("TASK_VERSION_CONFLICT");

            String savedTask = mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}",
                                fixture.projectId(), fixture.taskId())
                            .header(REQUESTER_ID, fixture.ownerId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            assertThat(objectMapper.readTree(savedTask).get("title").asText())
                    .isEqualTo(successBody.get("title").asText());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void updateRequiresANonNegativeVersion() throws Exception {
        TestFixture fixture = createFixture("version-validation");

        mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}",
                            fixture.projectId(), fixture.taskId())
                        .header(REQUESTER_ID, fixture.ownerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "버전 없는 수정",
                                  "description": "버전 검증",
                                  "status": "IN_PROGRESS",
                                  "assigneeId": %d
                                }
                                """.formatted(fixture.memberId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.version").exists());

        mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}",
                            fixture.projectId(), fixture.taskId())
                        .header(REQUESTER_ID, fixture.ownerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskUpdateBody(
                                "음수 버전 수정",
                                "IN_PROGRESS",
                                fixture.memberId(),
                                -1
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.version").exists());
    }

    private UpdateResult performConcurrentUpdate(
            TestFixture fixture,
            long requesterId,
            String title,
            String status,
            CountDownLatch ready,
            CountDownLatch start
    ) throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("동시 수정 시작 신호를 기다리는 데 실패했습니다.");
        }

        MvcResult result = mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}",
                            fixture.projectId(), fixture.taskId())
                        .header(REQUESTER_ID, requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskUpdateBody(
                                title,
                                status,
                                fixture.memberId(),
                                fixture.version()
                        )))
                .andReturn();
        return new UpdateResult(
                result.getResponse().getStatus(),
                result.getResponse().getContentAsString()
        );
    }

    private TestFixture createFixture(String prefix) throws Exception {
        long ownerId = createUser("동시성 소유자", prefix + "-owner@example.com");
        long memberId = createUser("동시성 담당자", prefix + "-member@example.com");
        long projectId = createProject(ownerId, prefix + " 동시성 프로젝트");
        addMember(ownerId, projectId, memberId);
        CreatedTask task = createTask(ownerId, projectId, memberId);
        return new TestFixture(ownerId, memberId, projectId, task.id(), task.version());
    }

    private long createUser(String name, String email) throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s"
                                }
                                """.formatted(name, email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createProject(long ownerId, String name) throws Exception {
        String response = mockMvc.perform(post("/api/projects")
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "동시성 통합 테스트"
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void addMember(long ownerId, long projectId, long memberId) throws Exception {
        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "role": "MEMBER"
                                }
                                """.formatted(memberId)))
                .andExpect(status().isCreated());
    }

    private CreatedTask createTask(long ownerId, long projectId, long assigneeId) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "동시 수정할 작업",
                                  "description": "낙관적 잠금을 검증한다",
                                  "status": "TODO",
                                  "assigneeId": %d
                                }
                                """.formatted(assigneeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return new CreatedTask(body.get("id").asLong(), body.get("version").asLong());
    }

    private String taskUpdateBody(
            String title,
            String status,
            long assigneeId,
            long version
    ) {
        return """
                {
                  "title": "%s",
                  "description": "동시 수정 결과",
                  "status": "%s",
                  "assigneeId": %d,
                  "version": %d
                }
                """.formatted(title, status, assigneeId, version);
    }

    private record CreatedTask(long id, long version) {
    }

    private record TestFixture(
            long ownerId,
            long memberId,
            long projectId,
            long taskId,
            long version
    ) {
    }

    private record UpdateResult(int status, String body) {
    }
}
