package sky.ch.booking.common.filter;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void doFilter_요청처리_응답헤더에requestId설정됨() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rooms");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(response.getHeader("X-Request-Id")).isNotBlank();
    }

    @Test
    void doFilter_요청처리완료후_MDC가정리됨() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rooms");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
}
