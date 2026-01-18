package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 权限信息表
 */
@Fory
@NoArgConstructor
data class Permission(

    /**
     * 权限id
     */
    var permissionId: Long? = null,

    /**
     * 权限字符串，三段式，module.menu.action,module模块，
     * 如system，auth，menu可以是一级菜单也可以是二级三级菜单，action即要执行的动作
     */
    var permissionCode: String? = null,

    /**
     * 权限名称
     */
    var permissionName: String? = null,

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