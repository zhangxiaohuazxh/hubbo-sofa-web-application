package cn.hubbo;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientForm;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class NettyHttpClientUnitTest {


	@Test
	public void testGetRequest() throws InterruptedException {
		String url = "https://v26-web-prime.douyinvod.com/video/tos/cn/tos-cn-vd-0026/oIwPbCA34hBgPIQ9GAD3Ci9wuAbOHiIaAE6AH/media-video-hvc1/?a=6383&ch=0&cr=8&dr=0&er=1&lr=default&cd=0%7C0%7C0%7C3&cv=1&br=1599&bt=1599&cs=4&ds=10&mime_type=video_mp4&qs=15&rc=ZWlkNjpnZDo6ODlkZGU2aUBpM3dlamo5cnFqNjMzNGkzM0BeY2MwYTIxX2IxLWJgM14tYSNrZWtvMmQ0MmRhLS1kLTBzcw%3D%3D&btag=c0000e00030000&cquery=101s_100o&dy_q=1762443139&expire=1762529817&l=202511062332189AD03EAC6AE76CD3E451&ply_type=4&policy=4&signature=46000a76b12c99557b20a9bbf34cb0d1&tk=webid&webid=3336119d6ecc0f2721002588cf880fbc4753b1582d4c54bc5f80aa9273463f02705544273fa3e4320f2dc5165808150c7fed41c540c56c0fb67d2ac4989635302814e45651bfbb26e63499686beaddcf3ca5ce2f878ae11bdaea942c250665ff61ef69416c71ebc37d0cec9c8eb6e36fa1a1abaaebf6fd7cd655cfd8276d18b20c37ff6ab0f3c7c56dbc0ad81c8bb0e5622c00ca915f87d3350ed13eeacf1fc41c06cf9456fbcf808c17c61a6c0ef7b72882f4023da1417b17e683ab3c52a170-422f33390b3e238497e57c1acb976460&fid=3d20f966234acd565c594b1fb31b25e6";
		HttpClient.create()
				.baseUrl(url)
				.get()
				.responseContent()
				.asString()
				.subscribe(System.out::println);
		Thread.sleep(3000);
	}

	@Test
	public void testUploadFile() throws InterruptedException {
		File uploadFile = new File("D:\\tmp\\cache\\test.txt");
		String res = HttpClient.create()
				.request(HttpMethod.POST)
				.uri("http://localhost:8080/test/file/upload")
				.sendForm((request, form) -> {
					HttpClientForm multipart = form.multipart(true);
					multipart.file("file", uploadFile);
					form.attr("attr", "test");
				})
				.responseSingle((response, byteBuf) -> {
					return byteBuf.asString(StandardCharsets.UTF_8);
				}).block();
		System.out.println(res);
	}

	@Test
	public void doPost() {
		ByteBuf byteBuf = Unpooled.directBuffer();
		HashMap<@Nullable Object, @Nullable Object> params = Maps.newHashMap();
		params.put("k1", 1);
		params.put("k2", 2);
		byteBuf.writeBytes(JSON.toJSONBytes(params));
		String res = HttpClient.create()
				.baseUrl("http://localhost:8080")
				.headers(headers -> {
					headers.add("Content-Type", "application/json");
				})
				.request(HttpMethod.POST)
				.uri("/test/request/body")
				.send(Mono.just(byteBuf))
				.responseContent()
				.aggregate()
				.asString(StandardCharsets.UTF_8)
				.block();
		System.out.println(res);
	}


}
