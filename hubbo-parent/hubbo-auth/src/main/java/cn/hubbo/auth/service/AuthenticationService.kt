package cn.hubbo.auth.service

import cn.hubbo.entity.auth.User
import cn.hubbo.entity.vo.ResultVO
import cn.hubbo.entity.vo.UserVO

interface AuthenticationService {

    fun login(): ResultVO<UserVO>


}