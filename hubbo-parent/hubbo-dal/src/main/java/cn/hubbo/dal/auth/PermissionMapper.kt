package cn.hubbo.dal.auth

import cn.hubbo.entity.auth.Permission
import com.mybatisflex.core.BaseMapper
import org.apache.ibatis.annotations.Mapper

@Mapper
interface PermissionMapper : BaseMapper<Permission> {


}