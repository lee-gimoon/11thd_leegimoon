package io.e2d.projectcollab.project.repository;

import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.domain.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectIdAndRole(Long projectId, ProjectRole role);

    @Query("""
            select pm
            from ProjectMember pm
            join fetch pm.project
            where pm.user.id = :userId
            order by pm.createdAt asc
            """)
    List<ProjectMember> findAllWithProjectByUserId(@Param("userId") Long userId);

    @Query("""
            select pm
            from ProjectMember pm
            join fetch pm.user
            where pm.project.id = :projectId
            order by pm.createdAt asc
            """)
    List<ProjectMember> findAllWithUserByProjectId(@Param("projectId") Long projectId);

    void deleteAllByProjectId(Long projectId);
}
