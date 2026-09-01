// 목적: 프로젝트 협업 애플리케이션을 실행하기 위해 만들어진 파일입니다.
// 역할: Spring Boot 애플리케이션의 시작점을 제공합니다.
package io.e2d.projectcollab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 역할: 프로젝트 협업 서버의 설정을 로드하고 애플리케이션을 시작합니다.
@SpringBootApplication
public class ProjectCollabApplication {

    // 역할: Spring Boot 런타임을 구동합니다.
    public static void main(String[] args) {
        SpringApplication.run(ProjectCollabApplication.class, args);
    }
}
