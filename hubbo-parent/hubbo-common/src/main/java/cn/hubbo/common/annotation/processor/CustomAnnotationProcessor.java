package cn.hubbo.common.annotation.processor;

import com.google.auto.service.AutoService;
import org.apache.commons.io.FileUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"cn.hubbo.common.annotation.Fory"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class CustomAnnotationProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		try {
			FileUtils.writeStringToFile(new File("D:\\tmp\\cache\\apt.txt"), "apt 处理器 init\r\n", StandardCharsets.UTF_8, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			FileUtils.writeStringToFile(new File("D:\\tmp\\cache\\apt.txt"), "apt 处理器开始执行\r\n", StandardCharsets.UTF_8, true);
			FileUtils.writeStringToFile(new File("D:\\tmp\\cache\\apt.txt"), annotations.toString(), StandardCharsets.UTF_8, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}


}
