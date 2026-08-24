package io.e2d.projectcollab.task.repository;

import io.e2d.projectcollab.task.domain.Task;
import io.e2d.projectcollab.task.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndProjectId(Long taskId, Long projectId);

    @Query(
            value = """
                    select task
                    from Task task
                    left join fetch task.assignee
                    where task.project.id = :projectId
                      and (:status is null or task.status = :status)
                      and (
                          :keyword is null
                          or lower(task.title) like concat('%', lower(:keyword), '%')
                          or lower(coalesce(task.description, '')) like concat('%', lower(:keyword), '%')
                      )
                    """,
            countQuery = """
                    select count(task)
                    from Task task
                    where task.project.id = :projectId
                      and (:status is null or task.status = :status)
                      and (
                          :keyword is null
                          or lower(task.title) like concat('%', lower(:keyword), '%')
                          or lower(coalesce(task.description, '')) like concat('%', lower(:keyword), '%')
                      )
                    """
    )
    Page<Task> search(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword,
            @Param("status") TaskStatus status,
            Pageable pageable
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Task task
            set task.assignee = null,
                task.version = task.version + 1
            where task.project.id = :projectId
              and task.assignee.id = :userId
            """)
    void clearAssigneeByProjectIdAndUserId(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId
    );

    void deleteAllByProjectId(Long projectId);
}
