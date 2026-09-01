// 목적: 프로젝트 생성·조회·수정·삭제 업무 규칙을 처리하기 위해 만들어진 파일입니다.
// 역할: 프로젝트 생명주기와 요청자 권한 및 연관 데이터 정리를 관리합니다.
package io.e2d.projectcollab.project.service;

import io.e2d.projectcollab.common.exception.ApiException;
import io.e2d.projectcollab.common.exception.ErrorCode;
import io.e2d.projectcollab.project.domain.Project;
import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.domain.ProjectRole;
import io.e2d.projectcollab.project.dto.ProjectDtos.CreateProjectRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ProjectResponse;
import io.e2d.projectcollab.project.dto.ProjectDtos.UpdateProjectRequest;
import io.e2d.projectcollab.project.repository.ProjectMemberRepository;
import io.e2d.projectcollab.project.repository.ProjectRepository;
import io.e2d.projectcollab.task.repository.TaskRepository;
import io.e2d.projectcollab.user.domain.User;
import io.e2d.projectcollab.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 역할: 프로젝트 기본 관리 흐름과 생성자의 소유자 등록을 담당합니다.
@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ProjectAuthorizationService authorizationService;

    // 역할: 프로젝트 관리에 필요한 저장소와 서비스 의존성을 주입받습니다.
    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository,
            UserService userService,
            ProjectAuthorizationService authorizationService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    // 역할: 요청자를 소유자로 등록하며 새 프로젝트를 생성합니다.
    @Transactional
    public ProjectResponse create(Long requesterId, CreateProjectRequest request) {
        User creator = userService.getEntity(requesterId);
        Project project = projectRepository.save(Project.create(
                request.name().trim(),
                normalizeDescription(request.description())
        ));
        projectMemberRepository.save(ProjectMember.create(project, creator, ProjectRole.OWNER));
        return ProjectResponse.from(project);
    }

    // 역할: 요청자가 참여 중인 프로젝트 목록을 조회합니다.
    public List<ProjectResponse> getMyProjects(Long requesterId) {
        userService.getEntity(requesterId);
        return projectMemberRepository.findAllWithProjectByUserId(requesterId).stream()
                .map(ProjectMember::getProject)
                .map(ProjectResponse::from)
                .toList();
    }

    // 역할: 요청자의 접근 권한을 확인하고 프로젝트 정보를 반환합니다.
    public ProjectResponse get(Long requesterId, Long projectId) {
        Project project = getEntity(projectId);
        authorizationService.requireMember(projectId, requesterId);
        return ProjectResponse.from(project);
    }

    // 역할: 요청자의 관리 권한을 확인하고 프로젝트 이름과 설명을 수정합니다.
    @Transactional
    public ProjectResponse update(Long requesterId, Long projectId, UpdateProjectRequest request) {
        Project project = getEntity(projectId);
        authorizationService.requireManager(projectId, requesterId);
        project.update(request.name().trim(), normalizeDescription(request.description()));
        return ProjectResponse.from(project);
    }

    // 역할: 요청자의 소유자 권한을 확인하고 프로젝트와 연관 데이터를 삭제합니다.
    @Transactional
    public void delete(Long requesterId, Long projectId) {
        Project project = getEntity(projectId);
        authorizationService.requireOwner(projectId, requesterId);
        taskRepository.deleteAllByProjectId(projectId);
        projectMemberRepository.deleteAllByProjectId(projectId);
        projectRepository.delete(project);
    }

    // 역할: 프로젝트 ID로 엔티티를 조회하고 없으면 프로젝트 없음 예외를 발생시킵니다.
    public Project getEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));
    }

    // 역할: 프로젝트 설명이 있으면 앞뒤 공백을 제거해 저장 형식으로 정규화합니다.
    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }
}
