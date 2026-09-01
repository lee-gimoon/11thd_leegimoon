// 목적: 작업 수정의 낙관적 잠금과 버전 검증을 통합 테스트하기 위해 만들어진 파일입니다.
// 역할: 순차·동시 수정 충돌, 저장 결과와 버전 입력 제약을 검증합니다.
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

// 역할: 실제 HTTP 요청과 병렬 실행을 통해 작업 동시성 제어의 5단계 요구사항을 검증합니다.
@SpringBootTest
@AutoConfigureMockMvc
class StageFiveTaskConcurrencyIntegrationTest {

    private static final String REQUESTER_ID = "X-Requester-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 역할: 오래된 버전의 후속 수정이 거부되고 첫 번째 수정 결과가 유지되는지 검증합니다.
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

    // 역할: 같은 버전으로 동시에 수정하면 하나만 성공하고 다른 하나는 충돌하는지 검증합니다.
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

    // 역할: 작업 수정 요청에 0 이상의 버전 값이 반드시 필요한지 검증합니다.
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

    // 역할: 시작 신호에 맞춰 지정한 요청자로 작업 수정 요청을 보내고 결과를 수집합니다.
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

    // 역할: 동시성 테스트에 필요한 소유자, 담당자, 프로젝트와 작업 묶음을 생성합니다.
    private TestFixture createFixture(String prefix) throws Exception {
        long ownerId = createUser("동시성 소유자", prefix + "-owner@example.com");
        long memberId = createUser("동시성 담당자", prefix + "-member@example.com");
        long projectId = createProject(ownerId, prefix + " 동시성 프로젝트");
        addMember(ownerId, projectId, memberId);
        CreatedTask task = createTask(ownerId, projectId, memberId);
        return new TestFixture(ownerId, memberId, projectId, task.id(), task.version());
    }

    // 역할: 동시성 테스트에 사용할 사용자를 API로 생성하고 식별자를 반환합니다.
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

    // 역할: 동시성 테스트에 사용할 프로젝트를 API로 생성하고 식별자를 반환합니다.
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

    // 역할: 동시성 테스트의 담당자를 프로젝트 일반 멤버로 추가합니다.
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

    // 역할: 동시 수정할 작업을 생성하고 식별자와 초기 버전을 반환합니다.
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

    // 역할: 제목, 상태, 담당자와 버전이 포함된 작업 수정 JSON 본문을 생성합니다.
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

    // 역할: 생성된 테스트 작업의 식별자와 초기 버전을 보관합니다.
    private record CreatedTask(long id, long version) {
    }

    // 역할: 동시성 테스트에 필요한 사용자, 프로젝트, 작업과 버전 정보를 보관합니다.
    private record TestFixture(
            long ownerId,
            long memberId,
            long projectId,
            long taskId,
            long version
    ) {
    }

    // 역할: 동시 수정 요청의 HTTP 상태와 응답 본문을 보관합니다.
    private record UpdateResult(int status, String body) {
    }
}
