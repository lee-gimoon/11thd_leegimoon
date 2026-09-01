// 목적: 프로젝트 정보를 데이터베이스 엔티티로 관리하기 위해 만들어진 파일입니다.
// 역할: 프로젝트 이름, 설명과 생성·수정 시각을 표현합니다.
package io.e2d.projectcollab.project.domain;

import io.e2d.projectcollab.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 역할: 협업 프로젝트의 영속 상태와 생성·수정 규칙을 정의합니다.
@Entity
@Table(name = "projects")
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    // 역할: JPA가 프로젝트 엔티티를 생성할 수 있도록 기본 생성자를 제공합니다.
    protected Project() {
    }

    // 역할: 이름과 설명으로 프로젝트 엔티티를 초기화합니다.
    private Project(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // 역할: 입력받은 이름과 설명을 가진 새 프로젝트 엔티티를 생성합니다.
    public static Project create(String name, String description) {
        return new Project(name, description);
    }

    // 역할: 프로젝트의 이름과 설명을 새 값으로 변경합니다.
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // 역할: 프로젝트의 식별자를 반환합니다.
    public Long getId() {
        return id;
    }

    // 역할: 프로젝트의 이름을 반환합니다.
    public String getName() {
        return name;
    }

    // 역할: 프로젝트의 설명을 반환합니다.
    public String getDescription() {
        return description;
    }
}
