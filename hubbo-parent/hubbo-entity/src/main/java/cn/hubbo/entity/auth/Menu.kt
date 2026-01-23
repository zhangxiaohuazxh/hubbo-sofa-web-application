package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import cn.hubbo.utils.fory.NoArgConstructor
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 菜单配置信息表，对应前端的路由信息
 */
@Fory
@NoArgConstructor
open class Menu(

    /**
     * 菜单编号
     */
    open val menuId: Long? = null,

    /**
     * 菜单名
     */
    var menuName: String? = null,

    /**
     * URI资源路径
     */
    var path: String? = null,

    /**
     * 渲染组件的路径
     */
    var component: String? = null,

    /**
     * 菜单层级
     */
    var level: Int? = null,

    /**
     * 标题
     */
    var title: String? = null,

    /**
     * 菜单icon
     */
    var icon: String? = null,

    /**
     * 是否需要认证
     */
    var auth: Boolean = true,

    /**
     * 是否缓存组件
     */
    var keepAlive: Boolean = true,

    /**
     * 序号，查询结果根据此字段排序
     */
    var displayOrder: Int? = null,

    /**
     * 菜单类型，如目录C，菜单M，超链接L
     */
    var menuType: String? = null,

    /**
     * 链接的地址，当menu_type为L时，此字段值有意义
     */
    var link: String? = null,

    /**
     * 父级菜单编号
     */
    var upperMenuId: Long? = null,

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