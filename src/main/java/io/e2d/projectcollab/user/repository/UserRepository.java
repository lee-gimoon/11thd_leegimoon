// 목적: 사용자 엔티티의 데이터베이스 접근 기능을 제공하기 위해 만들어진 파일입니다.
// 역할: 사용자 기본 CRUD와 이메일 중복 확인 쿼리를 제공합니다.
package io.e2d.projectcollab.user.repository;

import io.e2d.projectcollab.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

// 역할: 사용자 엔티티를 저장·조회하고 이메일 존재 여부를 확인합니다.
public interface UserRepository extends JpaRepository<User, Long> {

    // 역할: 대소문자를 구분하지 않고 같은 이메일의 사용자 존재 여부를 확인합니다.
    boolean existsByEmailIgnoreCase(String email);
}
