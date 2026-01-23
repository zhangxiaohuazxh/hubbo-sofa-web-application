package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 角色基础信息表
 */
@Fory
@NoArgConstructor
data class Role(

    /**
     * 角色id
     */
    var roleId: Long? = null,

    /**
     * 角色名称
     */
    var roleName: String? = null,

    /**
     * 是否启用
     */
    var enabled: Boolean = true,

    /**
     * 逻辑删除字段
     */
    var deleted: Boolean = false,

    /**
     * 创建人
     */
    var createBy: Long? = null,

    /**
     * 创建时间
     */
    var createTime: LocalDateTime? = null,

    /**
     * 更新人
     */
    var updateBy: Long? = null,

    /**
     * 更新时间
     */
    var updateTime: LocalDateTime? = null,

    /**
    租户id
     */
    var tenantId: Long? = null


) : Serializable
