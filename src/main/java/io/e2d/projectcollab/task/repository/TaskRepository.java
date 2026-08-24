package io.e2d.projectcollab.task.repository;

import io.e2d.projectcollab.task.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndProjectId(Long taskId, Long projectId);

    List<Task> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);

    void deleteAllByProjectId(Long projectId);
}
