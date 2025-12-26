package cn.hubbo.entity.vo

import cn.hubbo.entity.auth.Menu
import lombok.EqualsAndHashCode

@EqualsAndHashCode
data class MenuVO(
    override var menuId: Long? = null,
    /* 展示的名称 */
    var label: String? = null,
    /* 路径 */
    var key: String? = null,
    /* 子菜单 */
    var children: List<MenuVO>? = null
) : Menu() {


}
