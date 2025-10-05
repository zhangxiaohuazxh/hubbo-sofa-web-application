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
 * 按钮权限关联信息表
 */
@Fory
@NoArgConstructor
@Table("t_button_permission")
data class ButtonPermission(

    /**
     * 按钮权限编号
     */
    @Id(keyType = KeyType.None)
    @Column("id")
    var id: Long? = null,

    /**
     * 按钮权限字符
     */
    @Column("button_permission_code")
    var buttonPermissionCode: String? = null,

    /**
     * 按钮名称
     */
    @Column("button_name")
    var buttonName: String? = null,

    /**
     * 权限编号
     */
    @Column("permission_id")
    var permissionId: Int? = null,

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