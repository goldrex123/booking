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

    @Test
    void doFilter_체인실행도중_MDC에requestId가설정됨() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rooms");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] capturedRequestId = new String[1];
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                capturedRequestId[0] = MDC.get("requestId");
                super.doFilter(servletRequest, servletResponse);
            }
        };

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(capturedRequestId[0]).isNotNull();
        assertThat(capturedRequestId[0]).isEqualTo(response.getHeader("X-Request-Id"));
    }
}
