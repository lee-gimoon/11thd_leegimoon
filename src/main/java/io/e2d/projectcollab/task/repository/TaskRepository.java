// 목적: 작업 엔티티의 데이터베이스 접근과 검색 기능을 제공하기 위해 만들어진 파일입니다.
// 역할: 작업 조회·검색·담당자 해제·프로젝트별 삭제 쿼리를 제공합니다.
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

// 역할: 작업 엔티티를 저장·조회하고 조건 검색과 일괄 변경을 수행합니다.
public interface TaskRepository extends JpaRepository<Task, Long> {

    // 역할: 작업 ID와 프로젝트 ID가 모두 일치하는 작업을 조회합니다.
    Optional<Task> findByIdAndProjectId(Long taskId, Long projectId);

    // 역할: 프로젝트 작업을 검색어와 상태로 필터링해 페이지 단위로 조회합니다.
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

    // 역할: 프로젝트에서 제거될 사용자가 맡은 모든 작업의 담당자를 해제하고 버전을 올립니다.
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

    // 역할: 지정한 프로젝트에 속한 모든 작업을 삭제합니다.
    void deleteAllByProjectId(Long projectId);
}
