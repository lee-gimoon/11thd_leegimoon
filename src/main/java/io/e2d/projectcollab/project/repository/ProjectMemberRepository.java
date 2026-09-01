// 목적: 프로젝트 멤버 엔티티의 데이터베이스 접근 기능을 제공하기 위해 만들어진 파일입니다.
// 역할: 멤버십 조회·집계·삭제와 연관 엔티티 조회 쿼리를 제공합니다.
package io.e2d.projectcollab.project.repository;

import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.domain.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// 역할: 프로젝트 멤버 엔티티를 저장·조회하고 프로젝트별 멤버십을 관리합니다.
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // 역할: 지정한 프로젝트에 사용자가 이미 속해 있는지 확인합니다.
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    // 역할: 프로젝트 ID와 사용자 ID로 프로젝트 멤버를 조회합니다.
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    // 역할: 프로젝트에서 특정 역할을 가진 멤버 수를 계산합니다.
    long countByProjectIdAndRole(Long projectId, ProjectRole role);

    // 역할: 사용자가 참여한 모든 프로젝트를 멤버십 생성 순서로 함께 조회합니다.
    @Query("""
            select pm
            from ProjectMember pm
            join fetch pm.project
            where pm.user.id = :userId
            order by pm.createdAt asc
            """)
    List<ProjectMember> findAllWithProjectByUserId(@Param("userId") Long userId);

    // 역할: 프로젝트의 모든 멤버와 사용자 정보를 멤버십 생성 순서로 함께 조회합니다.
    @Query("""
            select pm
            from ProjectMember pm
            join fetch pm.user
            where pm.project.id = :projectId
            order by pm.createdAt asc
            """)
    List<ProjectMember> findAllWithUserByProjectId(@Param("projectId") Long projectId);

    // 역할: 지정한 프로젝트에 속한 모든 멤버십을 삭제합니다.
    void deleteAllByProjectId(Long projectId);
}
