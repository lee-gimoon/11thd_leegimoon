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

    protected Task() {
    }

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

    public static Task create(
            Project project,
            String title,
            String description,
            TaskStatus status,
            User assignee
    ) {
        return new Task(project, title, description, status, assignee);
    }

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

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public User getAssignee() {
        return assignee;
    }

    public Long getVersion() {
        return version;
    }
}
