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
 * 角色基础信息表
 */
@Fory
@NoArgConstructor
@Table("t_role")
data class Role(

    /**
     * 角色id
     */
    @Id(keyType = KeyType.None)
    @Column("role_id")
    var roleId: Long? = null,

    /**
     * 角色名称
     */
    @Column("role_name")
    var roleName: String? = null,

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
