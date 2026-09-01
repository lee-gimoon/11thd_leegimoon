// 목적: 사용자 정보를 데이터베이스 엔티티로 관리하기 위해 만들어진 파일입니다.
// 역할: 사용자의 식별자, 이름, 이메일과 생성·수정 시각을 표현합니다.
package io.e2d.projectcollab.user.domain;

import io.e2d.projectcollab.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// 역할: 협업 서비스에 등록된 사용자의 영속 상태와 생성 규칙을 정의합니다.
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    // 역할: JPA가 사용자 엔티티를 생성할 수 있도록 기본 생성자를 제공합니다.
    protected User() {
    }

    // 역할: 이름과 이메일로 사용자 엔티티를 초기화합니다.
    private User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // 역할: 입력받은 이름과 이메일을 가진 새 사용자 엔티티를 생성합니다.
    public static User create(String name, String email) {
        return new User(name, email);
    }

    // 역할: 사용자의 식별자를 반환합니다.
    public Long getId() {
        return id;
    }

    // 역할: 사용자의 이름을 반환합니다.
    public String getName() {
        return name;
    }

    // 역할: 사용자의 이메일을 반환합니다.
    public String getEmail() {
        return email;
    }
}
