// 목적: 시스템 상태 확인 API의 응답을 검증하기 위해 만들어진 파일입니다.
// 역할: 웹 계층에서 상태 코드와 애플리케이션 정보가 올바르게 반환되는지 테스트합니다.
package io.e2d.projectcollab.common.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 역할: SystemController만 로드해 상태 확인 엔드포인트의 계약을 검증합니다.
@WebMvcTest(SystemController.class)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 역할: 상태 확인 요청이 정상 상태와 애플리케이션 이름을 반환하는지 검증합니다.
    @Test
    void returnsApplicationStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("project-collab"));
    }
}
