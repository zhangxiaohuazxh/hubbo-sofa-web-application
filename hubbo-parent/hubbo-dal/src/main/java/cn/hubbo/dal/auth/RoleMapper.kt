package cn.hubbo.dal.auth

import cn.hubbo.entity.auth.Role
import com.mybatisflex.core.BaseMapper
import org.apache.ibatis.annotations.Mapper

@Mapper
interface RoleMapper : BaseMapper<Role> {


}