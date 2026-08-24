package io.e2d.projectcollab.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StageTwoApiIntegrationTest {

    private static final String REQUESTER_ID = "X-Requester-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void supportsBasicProjectMemberAndTaskCrudFlow() throws Exception {
        long ownerId = createUser("소유자", "owner@example.com");
        long memberId = createUser("멤버", "member@example.com");
        long projectId = createProject(ownerId, "채용 과제", "프로젝트 협업 서비스");

        mockMvc.perform(get("/api/projects")
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(projectId));

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(ownerId))
                .andExpect(jsonPath("$[0].role").value("OWNER"));

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "role": "MEMBER"
                                }
                                """.formatted(memberId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(memberId))
                .andExpect(jsonPath("$.role").value("MEMBER"));

        mockMvc.perform(patch("/api/projects/{projectId}/members/{userId}", projectId, memberId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        String createdTask = mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "REST API 구현",
                                  "description": "기본 CRUD 작성",
                                  "assigneeId": %d
                                }
                                """.formatted(memberId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assigneeId").value(memberId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long taskId = objectMapper.readTree(createdTask).get("id").asLong();

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId));

        mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "REST API 구현 완료",
                                  "description": "기본 CRUD 검증 완료",
                                  "status": "DONE",
                                  "assigneeId": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("REST API 구현 완료"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.assigneeId").doesNotExist());

        mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(delete("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/projects/{projectId}/members/{userId}", projectId, memberId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "채용 과제 수정",
                                  "description": "2단계 완료"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("채용 과제 수정"));

        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));
    }

    @Test
    void rejectsDuplicateEmailMemberAndInvalidInput() throws Exception {
        long ownerId = createUser("소유자", "unique@example.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "중복 사용자",
                                  "email": "UNIQUE@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        long projectId = createProject(ownerId, "중복 검증", null);
        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "role": "OWNER"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER_ALREADY_EXISTS"));
    }

    @Test
    void taskLookupUsesProjectBoundary() throws Exception {
        long userId = createUser("사용자", "boundary@example.com");
        long firstProjectId = createProject(userId, "첫 번째 프로젝트", null);
        long secondProjectId = createProject(userId, "두 번째 프로젝트", null);

        String createdTask = mockMvc.perform(post("/api/projects/{projectId}/tasks", firstProjectId)
                        .header(REQUESTER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "첫 번째 프로젝트 작업"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long taskId = objectMapper.readTree(createdTask).get("id").asLong();

        mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}", secondProjectId, taskId)
                        .header(REQUESTER_ID, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
    }

    @Test
    void returnsConsistentErrorWhenRequesterHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.path").value("/api/projects"));
    }

    @Test
    void exposesOpenApiTagsInUserWorkflowOrder() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0].name").value("Users"))
                .andExpect(jsonPath("$.tags[1].name").value("Projects"))
                .andExpect(jsonPath("$.tags[2].name").value("Project Members"))
                .andExpect(jsonPath("$.tags[3].name").value("Tasks"))
                .andExpect(jsonPath("$.tags[4].name").value("System"));
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
        JsonNode body = objectMapper.readTree(response);
        return body.get("id").asLong();
    }

    private long createProject(long requesterId, String name, String description) throws Exception {
        String descriptionJson = description == null
                ? "null"
                : objectMapper.writeValueAsString(description);
        String response = mockMvc.perform(post("/api/projects")
                        .header(REQUESTER_ID, requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": %s
                                }
                                """.formatted(name, descriptionJson)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
