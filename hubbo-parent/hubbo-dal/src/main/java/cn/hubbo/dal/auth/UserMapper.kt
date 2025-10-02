package cn.hubbo.dal.auth

import cn.hubbo.entity.auth.User
import com.mybatisflex.core.BaseMapper
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserMapper : BaseMapper<User> {

}
