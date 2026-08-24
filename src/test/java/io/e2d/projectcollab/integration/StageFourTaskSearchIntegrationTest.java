package io.e2d.projectcollab.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.e2d.projectcollab.task.domain.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StageFourTaskSearchIntegrationTest {

    private static final String REQUESTER_ID = "X-Requester-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void searchesTitleAndDescriptionAndCombinesStatusWithinProject() throws Exception {
        long ownerId = createUser("검색 사용자", "search-owner@example.com");
        long projectId = createProject(ownerId, "검색 프로젝트");
        long otherProjectId = createProject(ownerId, "다른 프로젝트");

        createTask(ownerId, projectId, "REST API 구현", "Spring Boot 백엔드", TaskStatus.TODO);
        createTask(ownerId, projectId, "React 화면 구현", "REST API 연동", TaskStatus.IN_PROGRESS);
        createTask(ownerId, projectId, "README 작성", "설계 근거 정리", TaskStatus.DONE);
        createTask(ownerId, otherProjectId, "REST API 외부 작업", "섞이면 안 되는 작업", TaskStatus.TODO);

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .param("keyword", "api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].projectId").value(projectId))
                .andExpect(jsonPath("$.content[1].projectId").value(projectId));

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("README 작성"))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .param("keyword", "rest")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("React 화면 구현"));

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .param("keyword", "Spring Boot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("REST API 구현"));

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void returnsStableNewestFirstPagesWithMetadata() throws Exception {
        long ownerId = createUser("페이지 사용자", "page-owner@example.com");
        long projectId = createProject(ownerId, "페이지 프로젝트");
        List<Long> taskIds = new ArrayList<>();

        for (int index = 1; index <= 5; index++) {
            taskIds.add(createTask(
                    ownerId,
                    projectId,
                    "페이지 작업 " + index,
                    "페이징 검증",
                    TaskStatus.TODO
            ));
        }

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(taskIds.get(4)))
                .andExpect(jsonPath("$.content[1].id").value(taskIds.get(3)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(taskIds.get(0)))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    @Test
    void rejectsInvalidPaginationAndStatusValues() throws Exception {
        long ownerId = createUser("검증 사용자", "page-validation@example.com");
        long projectId = createProject(ownerId, "페이지 검증 프로젝트");

        assertInvalidPagination(ownerId, projectId, "-1", "20");
        assertInvalidPagination(ownerId, projectId, "0", "0");
        assertInvalidPagination(ownerId, projectId, "0", "101");

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private void assertInvalidPagination(
            long requesterId,
            long projectId,
            String page,
            String size
    ) throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, requesterId)
                        .param("page", page)
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
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

    private long createProject(long requesterId, String name) throws Exception {
        String response = mockMvc.perform(post("/api/projects")
                        .header(REQUESTER_ID, requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "4단계 통합 테스트"
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createTask(
            long requesterId,
            long projectId,
            String title,
            String description,
            TaskStatus status
    ) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "%s",
                                  "status": "%s",
                                  "assigneeId": null
                                }
                                """.formatted(title, description, status.name())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
