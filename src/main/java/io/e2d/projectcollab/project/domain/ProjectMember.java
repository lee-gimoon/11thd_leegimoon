// 목적: 프로젝트와 사용자 사이의 멤버십을 데이터베이스에서 관리하기 위해 만들어진 파일입니다.
// 역할: 프로젝트 멤버의 소속 사용자와 역할 정보를 표현합니다.
package io.e2d.projectcollab.project.domain;

import io.e2d.projectcollab.common.domain.BaseTimeEntity;
import io.e2d.projectcollab.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// 역할: 프로젝트에 참여한 사용자와 부여된 역할의 영속 상태를 정의합니다.
@Entity
@Table(
        name = "project_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_members_project_user",
                columnNames = {"project_id", "user_id"}
        )
)
public class ProjectMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role;

    // 역할: JPA가 프로젝트 멤버 엔티티를 생성할 수 있도록 기본 생성자를 제공합니다.
    protected ProjectMember() {
    }

    // 역할: 프로젝트, 사용자, 역할로 프로젝트 멤버 엔티티를 초기화합니다.
    private ProjectMember(Project project, User user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = role;
    }

    // 역할: 지정한 프로젝트와 사용자 및 역할을 가진 새 멤버 엔티티를 생성합니다.
    public static ProjectMember create(Project project, User user, ProjectRole role) {
        return new ProjectMember(project, user, role);
    }

    // 역할: 프로젝트 멤버에게 부여된 역할을 변경합니다.
    public void changeRole(ProjectRole role) {
        this.role = role;
    }

    // 역할: 프로젝트 멤버 레코드의 식별자를 반환합니다.
    public Long getId() {
        return id;
    }

    // 역할: 멤버가 속한 프로젝트를 반환합니다.
    public Project getProject() {
        return project;
    }

    // 역할: 프로젝트에 참여한 사용자를 반환합니다.
    public User getUser() {
        return user;
    }

    // 역할: 프로젝트 멤버에게 부여된 역할을 반환합니다.
    public ProjectRole getRole() {
        return role;
    }
}
