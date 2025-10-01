package cn.hubbo.web.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/sysdate")
    public LocalDateTime localDateTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }

}
