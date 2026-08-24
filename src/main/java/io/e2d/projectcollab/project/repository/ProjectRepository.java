package io.e2d.projectcollab.project.repository;

import io.e2d.projectcollab.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
