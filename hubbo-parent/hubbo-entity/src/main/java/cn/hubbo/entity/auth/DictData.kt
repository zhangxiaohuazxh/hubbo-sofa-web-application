package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import java.sql.Timestamp
import java.time.LocalDateTime


@Fory
@NoArgConstructor
data class DictData(

    /**
     * 字典编号
     */
    var dictId: Long? = null,

    /**
     * 码值类型
     */
    var dictCode: String? = null,

    /**
     * 码值
     */
    var dictValue: String? = null,

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
     * 对用户的备注信息
     */
    var description: String? = null,

    /**
    租户id
     */
    var tenantId: Long? = null

) {


}