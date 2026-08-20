package cn.hubbo.common.utils;

import cn.hubbo.common.constants.LibraryConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

public final class TraceUtils {

	// 使用 AtomicLong，避免 AtomicInteger 在约 21 亿次后溢出为负数
	private static final AtomicLong COUNTER = new AtomicLong(1);

	private static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

	public static String getCurrentTraceId() {
		String traceId = MDC.get(LibraryConstants.TRACE_ID.getValue());
		if (StringUtils.isNotBlank(traceId)) {
			return traceId;
		}
		traceId = generateTraceId();
		MDC.put(LibraryConstants.TRACE_ID.getValue(), traceId);
		return traceId;
	}

	public static String generateTraceId() {
		StringBuilder buffer = new StringBuilder(48);
		appendHostOctets(buffer);
		buffer.append(System.currentTimeMillis()).append(PID).append(COUNTER.getAndIncrement());
		return buffer.toString();
	}

	/**
	 * 将本机 IPv4 地址的四段各转 2 位十六进制追加到 buffer。
	 * 若主机无 IPv4 地址（如 IPv6 环境）或解析失败，则降级使用主机名 hash，
	 * 避免对 IPv6 按 "." 拆分导致数组越界，也避免抛异常中断请求。
	 */
	private static void appendHostOctets(StringBuilder buffer) {
		String hostAddress = null;
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// 解析失败不中断 traceId 生成，降级为 hash 值
		}
		if (hostAddress != null) {
			String[] arr = hostAddress.split("\\.");
			if (arr.length == 4) {
				for (int i = 3; i >= 0; --i) {
					int id = Integer.parseInt(arr[3 - i]);
					buffer.append(String.format("%02x", id));
				}
				return;
			}
		}
		// 降级：用主机名 hash 的 8 位 hex 保证长度稳定且不越界
		String fallback = String.format("%08x", hostAddress == null
			? 0
			: hostAddress.hashCode());
		buffer.append(fallback);
	}

	/**
	 * 校验外部传入的 traceId 是否合法。
	 * <p>只允许字母/数字，长度 20~64（防止通过请求头伪造或注入非法字符）。</p>
	 *
	 * @param traceId 待校验的 traceId
	 * @return 合法返回 true
	 */
	public static boolean validTraceId(String traceId) {
		if (StringUtils.isBlank(traceId) || traceId.length() < 20 || traceId.length() > 64) {
			return false;
		}
		for (int i = 0; i < traceId.length(); i++) {
			char c = traceId.charAt(i);
			if (!Character.isLetterOrDigit(c)) {
				return false;
			}
		}
		return true;
	}

}