package cn.hubbo.web.test;

import cn.hubbo.entity.vo.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/test")
@RestController
@Slf4j
public class TestControllerJ {


	//@PostMapping("/file/upload")
	public ResultVO<?> fileUpload(MultipartFile file, String attr, HttpServletRequest request) {
		log.info("接收到的文件名 {} size {} attr {}", file.getName(), file.getSize(), attr);
		//log.info("file {} attr {}", file, attr);
		return ResultVO.success();
	}


}
