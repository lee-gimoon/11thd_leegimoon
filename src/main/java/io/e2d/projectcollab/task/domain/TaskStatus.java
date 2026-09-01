// 목적: 프로젝트 작업의 진행 상태를 정의하기 위해 만들어진 파일입니다.
// 역할: 할 일, 진행 중, 완료 상태를 열거형으로 제공합니다.
package io.e2d.projectcollab.task.domain;

// 역할: 작업이 현재 어느 진행 단계에 있는지 구분합니다.
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
