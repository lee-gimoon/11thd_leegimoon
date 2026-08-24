package io.e2d.projectcollab.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.e2d.projectcollab.project.domain.ProjectRole;
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
class StageThreeAuthorizationIntegrationTest {

    private static final String REQUESTER_ID = "X-Requester-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void nonMemberCannotAccessAnyProjectData() throws Exception {
        long ownerId = createUser("소유자", "access-owner@example.com");
        long outsiderId = createUser("외부인", "access-outsider@example.com");
        long projectId = createProject(ownerId, "접근 제한 프로젝트");
        long taskId = createTask(ownerId, projectId, null);

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .header(REQUESTER_ID, outsiderId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROJECT_ACCESS_DENIED"));

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .header(REQUESTER_ID, outsiderId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROJECT_ACCESS_DENIED"));

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, outsiderId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROJECT_ACCESS_DENIED"));

        mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, outsiderId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROJECT_ACCESS_DENIED"));

        mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, outsiderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "접근할 수 없는 작업"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROJECT_ACCESS_DENIED"));

        mockMvc.perform(get("/api/projects")
                        .header(REQUESTER_ID, outsiderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void ownerAdminAndMemberHaveDifferentProjectPermissions() throws Exception {
        long ownerId = createUser("소유자", "role-owner@example.com");
        long adminId = createUser("관리자", "role-admin@example.com");
        long memberId = createUser("멤버", "role-member@example.com");
        long candidateId = createUser("추가 멤버", "role-candidate@example.com");
        long projectId = createProject(ownerId, "역할 검증 프로젝트");
        addMember(ownerId, projectId, adminId, ProjectRole.ADMIN);
        addMember(ownerId, projectId, memberId, ProjectRole.MEMBER);

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .header(REQUESTER_ID, memberId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .header(REQUESTER_ID, memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectUpdateBody("멤버가 수정 시도")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROJECT_PERMISSION_DENIED"));

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header(REQUESTER_ID, memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody(candidateId, ProjectRole.MEMBER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROJECT_PERMISSION_DENIED"));

        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .header(REQUESTER_ID, adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectUpdateBody("관리자가 수정한 프로젝트")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("관리자가 수정한 프로젝트"));

        addMember(adminId, projectId, candidateId, ProjectRole.MEMBER);

        mockMvc.perform(delete("/api/projects/{projectId}/members/{userId}", projectId, candidateId)
                        .header(REQUESTER_ID, adminId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                        .header(REQUESTER_ID, adminId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROJECT_PERMISSION_DENIED"));
    }

    @Test
    void taskCanBeCreatedByAnyMemberButModifiedOnlyByAssigneeOrManager() throws Exception {
        long ownerId = createUser("소유자", "task-owner@example.com");
        long adminId = createUser("관리자", "task-admin@example.com");
        long assigneeId = createUser("담당자", "task-assignee@example.com");
        long otherMemberId = createUser("다른 멤버", "task-other@example.com");
        long projectId = createProject(ownerId, "작업 권한 프로젝트");
        addMember(ownerId, projectId, adminId, ProjectRole.ADMIN);
        addMember(ownerId, projectId, assigneeId, ProjectRole.MEMBER);
        addMember(ownerId, projectId, otherMemberId, ProjectRole.MEMBER);

        long taskId = createTask(otherMemberId, projectId, assigneeId);

        mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, otherMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value(assigneeId));

        mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, otherMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskUpdateBody("다른 멤버의 수정 시도", assigneeId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TASK_PERMISSION_DENIED"));

        mockMvc.perform(delete("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, otherMemberId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TASK_PERMISSION_DENIED"));

        mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, assigneeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskUpdateBody("담당자가 수정한 작업", assigneeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("담당자가 수정한 작업"));

        mockMvc.perform(delete("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, adminId))
                .andExpect(status().isNoContent());
    }

    @Test
    void taskAssigneeMustBeProjectMember() throws Exception {
        long ownerId = createUser("소유자", "assignee-owner@example.com");
        long memberId = createUser("멤버", "assignee-member@example.com");
        long outsiderId = createUser("외부인", "assignee-outsider@example.com");
        long projectId = createProject(ownerId, "담당자 검증 프로젝트");
        addMember(ownerId, projectId, memberId, ProjectRole.MEMBER);

        mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskCreateBody(outsiderId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSIGNEE_NOT_PROJECT_MEMBER"));

        long taskId = createTask(ownerId, projectId, memberId);

        mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskUpdateBody("외부인에게 재지정", outsiderId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSIGNEE_NOT_PROJECT_MEMBER"));

        mockMvc.perform(delete("/api/projects/{projectId}/members/{userId}", projectId, memberId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(REQUESTER_ID, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").doesNotExist())
                .andExpect(jsonPath("$.assigneeName").doesNotExist())
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void projectAlwaysKeepsAtLeastOneOwner() throws Exception {
        long ownerId = createUser("기존 소유자", "owner-rule-owner@example.com");
        long adminId = createUser("관리자", "owner-rule-admin@example.com");
        long projectId = createProject(ownerId, "OWNER 유지 프로젝트");
        addMember(ownerId, projectId, adminId, ProjectRole.ADMIN);

        mockMvc.perform(patch("/api/projects/{projectId}/members/{userId}", projectId, ownerId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody(ProjectRole.MEMBER)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_OWNER_REQUIRED"));

        mockMvc.perform(delete("/api/projects/{projectId}/members/{userId}", projectId, ownerId)
                        .header(REQUESTER_ID, adminId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_OWNER_REQUIRED"));

        mockMvc.perform(patch("/api/projects/{projectId}/members/{userId}", projectId, adminId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody(ProjectRole.OWNER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OWNER"));

        mockMvc.perform(patch("/api/projects/{projectId}/members/{userId}", projectId, ownerId)
                        .header(REQUESTER_ID, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody(ProjectRole.MEMBER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"));

        mockMvc.perform(delete("/api/projects/{projectId}/members/{userId}", projectId, ownerId)
                        .header(REQUESTER_ID, adminId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .header(REQUESTER_ID, adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(adminId))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
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
                        .content(projectUpdateBody(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void addMember(
            long requesterId,
            long projectId,
            long userId,
            ProjectRole role
    ) throws Exception {
        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header(REQUESTER_ID, requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberBody(userId, role)))
                .andExpect(status().isCreated());
    }

    private long createTask(long requesterId, long projectId, Long assigneeId) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .header(REQUESTER_ID, requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskCreateBody(assigneeId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String projectUpdateBody(String name) {
        return """
                {
                  "name": "%s",
                  "description": "권한 통합 테스트"
                }
                """.formatted(name);
    }

    private String memberBody(long userId, ProjectRole role) {
        return """
                {
                  "userId": %d,
                  "role": "%s"
                }
                """.formatted(userId, role.name());
    }

    private String roleBody(ProjectRole role) {
        return """
                {
                  "role": "%s"
                }
                """.formatted(role.name());
    }

    private String taskCreateBody(Long assigneeId) {
        String assigneeValue = assigneeId == null ? "null" : assigneeId.toString();
        return """
                {
                  "title": "권한 검증 작업",
                  "description": "작업 권한을 검증한다",
                  "status": "TODO",
                  "assigneeId": %s
                }
                """.formatted(assigneeValue);
    }

    private String taskUpdateBody(String title, Long assigneeId) {
        String assigneeValue = assigneeId == null ? "null" : assigneeId.toString();
        return """
                {
                  "title": "%s",
                  "description": "수정 권한을 검증한다",
                  "status": "IN_PROGRESS",
                  "assigneeId": %s,
                  "version": 0
                }
                """.formatted(title, assigneeValue);
    }
}
