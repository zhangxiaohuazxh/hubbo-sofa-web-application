package cn.hubbo.dal

import cn.hubbo.dal.tables.TProject.Companion.T_PROJECT
import cn.hubbo.dal.tables.TProjectIteration.Companion.T_PROJECT_ITERATION
import cn.hubbo.dal.tables.records.TProjectIterationRecord
import cn.hubbo.dal.tables.records.TProjectRecord
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class IntegrationDao(val dsl: DSLContext) {


    suspend fun findProjectIntegrationInfoByIterationId(id: Long): TProjectIterationRecord? {
        return Mono.from(
            dsl.selectFrom(T_PROJECT_ITERATION)
                .where(
                    T_PROJECT_ITERATION.ITERATION_ID.eq(id)
                )
        ).map { it.into(TProjectIterationRecord::class.java) }
            .awaitSingleOrNull()
    }

    suspend fun findProjectInfoByProjectId(projectId: Long): TProjectRecord? {
        return Mono.from(
            dsl.selectFrom(T_PROJECT)
                .where(T_PROJECT.PROJECT_ID.eq(projectId))
        ).map { it.into(TProjectRecord::class.java) }
            .awaitSingleOrNull()
    }


}