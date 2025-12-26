package cn.hubbo.web.filter;

import cn.hubbo.common.constants.LibraryConstants;
import cn.hubbo.common.utils.TraceUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TraceInterceptor extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = request.getHeader(LibraryConstants.TRACE_ID.getValue());
            if (StringUtils.isBlank(traceId)) {
                traceId = TraceUtils.generateTraceId();
            } else {
                TraceUtils.validTraceId(traceId);
            }
            MDC.put(LibraryConstants.TRACE_ID.getValue(), traceId);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } finally {
            MDC.remove(LibraryConstants.TRACE_ID.getValue());
        }
    }
}
