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
 * 菜单配置信息表，对应前端的路由信息
 */
@Fory
@NoArgConstructor
@Table("t_menu")
open class Menu(

    /**
     * 菜单编号
     */
    @Id(keyType = KeyType.None)
    @Column("menu_id")
    var menuId: Long? = null,

    /**
     * 菜单名
     */
    @Column("menu_name")
    var menuName: String? = null,

    /**
     * URI资源路径
     */
    @Column("path")
    var path: String? = null,

    /**
     * 渲染组件的路径
     */
    @Column("component")
    var component: String? = null,

    /**
     * 菜单层级
     */
    @Column("level")
    var level: Int? = null,

    /**
     * 标题
     */
    @Column("title")
    var title: String? = null,

    /**
     * 菜单icon
     */
    @Column("icon")
    var icon: String? = null,

    /**
     * 是否需要认证
     */
    @Column("auth")
    var auth: Boolean = true,

    /**
     * 是否缓存组件
     */
    @Column("keep_alive")
    var keepAlive: Boolean = true,

    /**
     * 序号，查询结果根据此字段排序
     */
    @Column("display_order")
    var displayOrder: Int? = null,

    /**
     * 菜单类型，如目录C，菜单M，超链接L
     */
    @Column("menu_type")
    var menuType: String? = null,

    /**
     * 链接的地址，当menu_type为L时，此字段值有意义
     */
    @Column("link")
    var link: String? = null,

    /**
     * 父级菜单编号
     */
    @Column("upper_menu_id")
    var upperMenuId: Long? = null,

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
    var updateTime: LocalDateTime? = null,

    /**
    租户id
     */
    @Column("tenant_id")
    var tenantId: Long? = null


) : Serializable