package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import com.mybatisflex.annotation.Column
import com.mybatisflex.annotation.Id
import com.mybatisflex.annotation.KeyType
import com.mybatisflex.annotation.Table
import lombok.EqualsAndHashCode
import java.io.Serializable
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * 用户基础信息表
 */
@Fory
@NoArgConstructor
@Table("t_user")
@EqualsAndHashCode
open class User(

    /**
     * 用户编号，分布式id
     */
    @Id(keyType = KeyType.None)
    @Column("user_id")
    var userId: Long? = null,

    /**
     * 用户名
     */
    @Column("user_name")
    var userName: String? = null,

    /**
     * 手机号
     */
    @Column("phone")
    var phone: String? = null,

    /**
     * 用户密码，不要存储明文
     */
    @Column("passwd")
    var passwd: String? = null,

    /**
     * 头像url地址
     */
    @Column("profile_url")
    var profileUrl: String? = null,

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
     * 创建人，用户自行注册的话值就是自己的id
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
    var updateTime: Timestamp? = null,

    /**
     * 最近一次的上线时间
     */
    @Column("recent_online_time")
    var recentOnlineTime: Timestamp? = null,

    /**
     * 对用户的备注信息
     */
    @Column("description")
    var description: String? = null,

    /**
    租户id
     */
    @Column("tenant_id")
    var tenantId: Long? = null

) : Serializable