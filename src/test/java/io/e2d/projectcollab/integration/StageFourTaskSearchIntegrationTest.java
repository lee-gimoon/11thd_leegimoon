// 목적: 작업 검색·필터·페이지 조회 기능을 통합 검증하기 위해 만들어진 파일입니다.
// 역할: 검색 범위, 정렬 안정성, 페이지 메타데이터와 잘못된 조건 처리를 테스트합니다.
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

// 역할: 실제 API 호출을 통해 작업 검색과 페이지 처리의 4단계 요구사항을 검증합니다.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StageFourTaskSearchIntegrationTest {

    private static final String REQUESTER_ID = "X-Requester-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 역할: 제목·설명 검색과 상태 필터가 프로젝트 범위 안에서 함께 적용되는지 검증합니다.
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

    // 역할: 작업이 최신순으로 안정되게 나뉘고 정확한 페이지 정보가 반환되는지 검증합니다.
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

    // 역할: 허용 범위를 벗어난 페이지 값과 알 수 없는 상태 값이 거부되는지 검증합니다.
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

    // 역할: 지정한 페이지 조건으로 요청해 페이지 유효성 오류가 반환되는지 검증합니다.
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

    // 역할: 검색 테스트에 사용할 사용자를 API로 생성하고 식별자를 반환합니다.
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

    // 역할: 검색 테스트에 사용할 프로젝트를 API로 생성하고 식별자를 반환합니다.
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

    // 역할: 검색 조건과 페이지 정렬을 검증할 작업을 생성하고 식별자를 반환합니다.
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
