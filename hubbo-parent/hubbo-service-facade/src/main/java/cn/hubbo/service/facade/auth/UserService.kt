package cn.hubbo.service.facade.auth

import cn.hubbo.dal.auth.UserMapper
import cn.hubbo.entity.auth.User
import com.mybatisflex.spring.service.impl.ServiceImpl
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

/**
 * 明确不需要多态 复杂逻辑的类直接实现 不再定义接口
 */
@Slf4j
@Service
open class UserService : ServiceImpl<UserMapper, User>() {


}