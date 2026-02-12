package cn.hubbo.dal


import cn.hubbo.dal.tables.pojos.TMenu
import cn.hubbo.dal.tables.references.T_MENU
import kotlinx.coroutines.reactor.awaitSingle
import org.jooq.DSLContext
import org.jooq.Records
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class MenuDao(val dsl: DSLContext) {

    suspend fun findAllMenus(): List<TMenu> {
        return Flux.from(
            dsl.select(
                T_MENU.MENU_ID, T_MENU.MENU_NAME, T_MENU.PATH,
                T_MENU.COMPONENT, T_MENU.LEVEL, T_MENU.TITLE,
                T_MENU.ICON, T_MENU.AUTH, T_MENU.KEEP_ALIVE,
                T_MENU.DISPLAY_ORDER, T_MENU.MENU_TYPE, T_MENU.LINK,
                T_MENU.UPPER_MENU_ID, T_MENU.ENABLED, T_MENU.DELETED,
                T_MENU.CREATE_BY, T_MENU.CREATE_TIME, T_MENU.UPDATE_BY,
                T_MENU.UPDATE_TIME, T_MENU.TENANT_ID
            ).from(T_MENU)
                .where(T_MENU.DELETED.eq(false))
                .and(T_MENU.ENABLED.eq(true))
                .and(T_MENU.TENANT_ID.eq(1L))
        ).map(Records.mapping(::TMenu))
            .collectList()
            .awaitSingle()
    }


}