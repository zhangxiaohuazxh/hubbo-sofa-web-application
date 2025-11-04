package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import com.mybatisflex.annotation.Column
import com.mybatisflex.annotation.Id
import com.mybatisflex.annotation.Table
import java.sql.Timestamp
import java.time.LocalDateTime


@Fory
@Table("t_dict_data")
@NoArgConstructor
data class DictData(

    /**
     * 字典编号
     */
    @Id
    @Column("dict_id")
    var dictId: Long? = null,

    /**
     * 码值类型
     */
    @Column("dict_code")
    var dictCode: String? = null,

    /**
     * 码值
     */
    @Column("dict_value")
    var dictValue: String? = null,

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
     * 对用户的备注信息
     */
    @Column("description")
    var description: String? = null,

    /**
    租户id
     */
    @Column("tenant_id")
    var tenantId: Long? = null

) {


}