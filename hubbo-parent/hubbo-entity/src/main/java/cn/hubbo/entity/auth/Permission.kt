package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import com.mybatisflex.annotation.Column
import com.mybatisflex.annotation.Id
import com.mybatisflex.annotation.KeyType
import com.mybatisflex.annotation.Table
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 权限信息表
 */
@Fory
@NoArgConstructor
@Table("t_permission")
data class Permission(

    /**
     * 权限id
     */
    @Id(keyType = KeyType.None)
    @Column("permission_id")
    var permissionId: Long? = null,

    /**
     * 权限字符串，三段式，module.menu.action,module模块，
     * 如system，auth，menu可以是一级菜单也可以是二级三级菜单，action即要执行的动作
     */
    @Column("permission_code")
    var permissionCode: String? = null,

    /**
     * 权限名称
     */
    @Column("permission_name")
    var permissionName: String? = null,

    /**
     * 是否启用
     */
    @Column("enabled")
    var enabled: Boolean = true,

    /**
     * 逻辑删除字段
     */
    @Column("deleted")
    var deleted: Boolean = false,

    /**
     * 创建人
     */
    @Column("create_by")
    var createBy: Long? = null,

    /**
     * 创建时间
     */
    @Column("create_time")
    var createTime: LocalDateTime? = null,

    /**
     * 更新人
     */
    @Column("update_by")
    var updateBy: Long? = null,

    /**
     * 更新时间
     */
    @Column("update_time")
    var updateTime: LocalDateTime? = null

) : Serializable