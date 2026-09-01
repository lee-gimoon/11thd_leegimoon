// 목적: 요청자 식별에 사용하는 HTTP 헤더 이름을 공통 관리하기 위해 만들어진 파일입니다.
// 역할: 요청자 ID 헤더 상수를 제공합니다.
package io.e2d.projectcollab.common.web;

// 역할: 요청자 관련 HTTP 헤더 이름을 상수로 노출하는 유틸리티 클래스입니다.
public final class RequesterHeaders {

    public static final String REQUESTER_ID = "X-Requester-Id";

    // 역할: 인스턴스 생성을 막아 상수 전용 클래스로 사용되게 합니다.
    private RequesterHeaders() {
    }
}
