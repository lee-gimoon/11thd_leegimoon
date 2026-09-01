// 목적: 프로젝트 협업 애플리케이션의 기본 구동 여부를 검증하기 위해 만들어진 파일입니다.
// 역할: Spring 애플리케이션 컨텍스트가 정상적으로 로드되는지 테스트합니다.
package io.e2d.projectcollab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 역할: 전체 Spring 설정과 빈 구성이 문제없이 초기화되는지 확인합니다.
@SpringBootTest
class ProjectCollabApplicationTests {

    // 역할: 애플리케이션 컨텍스트 로딩 과정에서 예외가 발생하지 않는지 검증합니다.
    @Test
    void contextLoads() {
    }
}
