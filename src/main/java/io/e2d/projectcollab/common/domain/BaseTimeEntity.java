// 목적: 엔티티의 생성·수정 시간을 공통으로 관리하기 위해 만들어진 파일입니다.
// 역할: 하위 엔티티에 생성 시각과 수정 시각 및 자동 갱신 기능을 제공합니다.
package io.e2d.projectcollab.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

// 역할: 모든 영속 엔티티가 공유하는 생성·수정 시각 필드와 생명주기 처리를 정의합니다.
@MappedSuperclass
public abstract class BaseTimeEntity {

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 역할: 엔티티가 처음 저장될 때 생성 시각과 수정 시각을 현재 시각으로 설정합니다.
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // 역할: 엔티티가 갱신될 때 수정 시각을 현재 시각으로 변경합니다.
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 역할: 엔티티가 생성된 시각을 반환합니다.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 역할: 엔티티가 마지막으로 수정된 시각을 반환합니다.
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
