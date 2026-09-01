// 목적: 프로젝트 멤버에게 부여할 수 있는 역할을 정의하기 위해 만들어진 파일입니다.
// 역할: 소유자, 관리자, 일반 멤버 역할을 열거형으로 제공합니다.
package io.e2d.projectcollab.project.domain;

// 역할: 프로젝트 안에서 사용자가 갖는 권한 수준을 구분합니다.
public enum ProjectRole {
    OWNER,
    ADMIN,
    MEMBER
}
