package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 按钮权限关联信息表
 */
@Fory
@NoArgConstructor
data class ButtonPermission(

    /**
     * 按钮权限编号
     */
    var id: Long? = null,

    /**
     * 按钮权限字符
     */
    var buttonPermissionCode: String? = null,

    /**
     * 按钮名称
     */
    var buttonName: String? = null,

    /**
     * 权限编号
     */
    var permissionId: Int? = null,

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