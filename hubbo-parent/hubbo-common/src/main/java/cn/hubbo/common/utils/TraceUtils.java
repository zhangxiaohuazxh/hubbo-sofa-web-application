package cn.hubbo.common.utils;

import cn.hubbo.common.constants.LibraryConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public final class TraceUtils {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    public static String getCurrentTraceId() {
        String traceId = MDC.get(LibraryConstants.TRACE_ID.getValue());
        if (StringUtils.isNotBlank(traceId)) {
            return traceId;
        }
        traceId = generateTraceId();
        MDC.put(LibraryConstants.TRACE_ID.getValue(), traceId);
        return "";
    }

    public static String generateTraceId() {
        String[] arr;
        try {
            arr = InetAddress.getLocalHost().getHostAddress().split("\\.");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 3; i >= 0; --i) {
            Integer id = Integer.parseInt(arr[3 - i]);
            buffer.append(String.format("%02x", id));
        }
        buffer.append(System.currentTimeMillis()).append(PID).append(COUNTER.getAndIncrement());
        return buffer.toString();
    }

    public static void validTraceId(String traceId) {
        // todo
    }

}
