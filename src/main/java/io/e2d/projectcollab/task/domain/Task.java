// 목적: 프로젝트 작업 정보를 데이터베이스 엔티티로 관리하기 위해 만들어진 파일입니다.
// 역할: 작업의 내용, 상태, 담당자와 동시성 버전을 표현합니다.
package io.e2d.projectcollab.task.domain;

import io.e2d.projectcollab.common.domain.BaseTimeEntity;
import io.e2d.projectcollab.project.domain.Project;
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
import jakarta.persistence.Version;

// 역할: 프로젝트 작업의 영속 상태와 생성·수정 규칙을 정의합니다.
@Entity
@Table(name = "tasks")
public class Task extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Version
    @Column(nullable = false)
    private Long version;

    // 역할: JPA가 작업 엔티티를 생성할 수 있도록 기본 생성자를 제공합니다.
    protected Task() {
    }

    // 역할: 프로젝트, 내용, 상태, 담당자로 작업 엔티티를 초기화합니다.
    private Task(
            Project project,
            String title,
            String description,
            TaskStatus status,
            User assignee
    ) {
        this.project = project;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignee = assignee;
    }

    // 역할: 지정한 프로젝트와 작업 정보로 새 작업 엔티티를 생성합니다.
    public static Task create(
            Project project,
            String title,
            String description,
            TaskStatus status,
            User assignee
    ) {
        return new Task(project, title, description, status, assignee);
    }

    // 역할: 작업의 제목, 설명, 상태와 담당자를 새 값으로 변경합니다.
    public void update(
            String title,
            String description,
            TaskStatus status,
            User assignee
    ) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignee = assignee;
    }

    // 역할: 작업의 식별자를 반환합니다.
    public Long getId() {
        return id;
    }

    // 역할: 작업이 속한 프로젝트를 반환합니다.
    public Project getProject() {
        return project;
    }

    // 역할: 작업의 제목을 반환합니다.
    public String getTitle() {
        return title;
    }

    // 역할: 작업의 설명을 반환합니다.
    public String getDescription() {
        return description;
    }

    // 역할: 작업의 현재 진행 상태를 반환합니다.
    public TaskStatus getStatus() {
        return status;
    }

    // 역할: 작업 담당자를 반환합니다.
    public User getAssignee() {
        return assignee;
    }

    // 역할: 낙관적 잠금에 사용하는 작업 버전을 반환합니다.
    public Long getVersion() {
        return version;
    }
}
