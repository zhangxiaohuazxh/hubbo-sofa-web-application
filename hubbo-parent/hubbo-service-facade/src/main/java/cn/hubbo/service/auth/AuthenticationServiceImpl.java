package cn.hubbo.service.auth;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@SofaService(interfaceType = AuthenticationService.class)
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {


}
