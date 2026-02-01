package cn.hubbo.dal

import cn.hubbo.dal.tables.TUser.Companion.T_USER
import cn.hubbo.dal.tables.pojos.TUser
import cn.hubbo.dal.tables.references.T_ROLE
import cn.hubbo.dal.tables.references.T_USER_ROLE
import cn.hubbo.entity.auth.SystemUserInfo
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.impl.DSL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class UserDao(val dsl: DSLContext) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(UserDao::class.java) }

    /**
     * 最轻量级的反射方案
     *
     * Records.mapping依赖属性的顺序赋值，保证查询的顺序和构造函数中参数的顺序一致，类型也一致，否则可能会出现值错位，甚至类型不匹配
     * 而且查询出的列的数量要和构造函数的参数个数一致，否则编译会报错
     * 可以使用 DSL.inline('1').`as`("status") 凑数
     */
    suspend fun findByUsernameLike(username: String): MutableList<TUser?> {
        return Flux.from(
            dsl.select(
                T_USER.USER_ID,
                T_USER.USER_NAME,
                T_USER.PHONE,
                T_USER.PASSWD,
                T_USER.PROFILE_URL,
                T_USER.ENABLED,
                T_USER.DELETED,
                T_USER.CREATE_BY,
                T_USER.CREATE_TIME,
                T_USER.UPDATE_BY,
                T_USER.UPDATE_TIME,
                T_USER.RECENT_ONLINE_TIME,
                T_USER.DESCRIPTION,
                T_USER.TENANT_ID
            ).from(T_USER)
                .where(T_USER.USER_NAME.contains(username))
        ).map(Records.mapping(::TUser))
            .collectList()
            .map { it.toMutableList() }
            .awaitSingle()
    }


    /**
     * 可能查不到数据时使用awaitSingleOrNull，list时最好使用collectList，这样可以保证mono里有元素，查不到时元素为emptyList
     * 反射略重于上面的方案
     */
    suspend fun findByUsernameEquals(username: String): TUser? {
        return Mono.from(
            dsl.selectFrom(T_USER)
                .where(T_USER.USER_NAME.eq(username))
        )
            .map { it.into(TUser::class.java) }
            .awaitSingleOrNull()
    }

    /**
     * 扁平化查询，适用于简单查询，数据量不大的情况，CPU负担相对来说较小
     */
    suspend fun findUserRolesByUserId(userId: Long): MutableList<SystemUserInfo> {
        return Flux.from(
            dsl.select(T_USER.USER_ID, T_USER.USER_NAME, T_ROLE.ROLE_ID, T_ROLE.ROLE_NAME)
                .from(T_USER)
                .join(T_USER_ROLE)
                .on(T_USER_ROLE.USER_ID.eq(T_USER.USER_ID))
                .and(T_USER_ROLE.ENABLED.eq(true))
                .and(T_USER_ROLE.DELETED.eq(false))
                .join(T_ROLE)
                .on(T_USER_ROLE.ROLE_ID.eq(T_ROLE.ROLE_ID))
                .and(T_ROLE.ENABLED.eq(true))
                .and(T_ROLE.DELETED.eq(false))
                .where(T_USER.USER_ID.eq(userId))
        ).map(Records.mapping(::SystemUserInfo))
            .collectList()
            .awaitSingle()
    }


    /**
     * Multiset优势：
     * 1. 符合直觉？ 嵌套的数据结构
     * 2. 数据量大的情况下，可以节省网络带宽并降低内存中对象的创建开销
     *  适用于嵌套的数据结构，且支持JSON聚合函数的数据库，数据库本身CPU负载高的情况下就不要使用了
     */
    // 全量查询所有的用户和角色信息
    suspend fun findUserRoleList() {
//        dsl.select(T_USER.USER_ID, T_USER.USER_NAME, DSL.multiset())
//            .from(T_USER)
    }


}
