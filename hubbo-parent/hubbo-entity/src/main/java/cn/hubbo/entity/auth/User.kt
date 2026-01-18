package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import lombok.EqualsAndHashCode
import java.io.Serializable
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * 用户基础信息表
 */
@Fory
@NoArgConstructor
@EqualsAndHashCode
open class User(

    /**
     * 用户编号，分布式id
     */
    var userId: Long? = null,

    /**
     * 用户名
     */
    var userName: String? = null,

    /**
     * 手机号
     */
    var phone: String? = null,

    /**
     * 用户密码，不要存储明文
     */
    var passwd: String? = null,

    /**
     * 头像url地址
     */
    var profileUrl: String? = null,

    /**
     * 是否启用
     */
    var enabled: Boolean = true,

    /**
     * 逻辑删除字段
     */
    var deleted: Boolean = false,

    /**
     * 创建人，用户自行注册的话值就是自己的id
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
    var updateTime: Timestamp? = null,

    /**
     * 最近一次的上线时间
     */
    var recentOnlineTime: Timestamp? = null,

    /**
     * 对用户的备注信息
     */
    var description: String? = null,

    /**
    租户id
     */
    var tenantId: Long? = null

) : Serializable