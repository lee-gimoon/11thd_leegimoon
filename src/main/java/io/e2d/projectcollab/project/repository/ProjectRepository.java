// 목적: 프로젝트 엔티티의 데이터베이스 접근 기능을 제공하기 위해 만들어진 파일입니다.
// 역할: 프로젝트의 기본 저장·조회·수정·삭제 기능을 제공합니다.
package io.e2d.projectcollab.project.repository;

import io.e2d.projectcollab.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

// 역할: 프로젝트 엔티티에 대한 JPA 기본 CRUD 작업을 수행합니다.
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
