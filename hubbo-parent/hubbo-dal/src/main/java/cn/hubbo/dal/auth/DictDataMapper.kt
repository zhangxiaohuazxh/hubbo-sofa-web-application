package cn.hubbo.dal.auth

import cn.hubbo.entity.auth.DictData
import com.mybatisflex.core.BaseMapper
import org.apache.ibatis.annotations.Mapper

@Mapper
interface DictDataMapper : BaseMapper<DictData> {

    /**
     * 查询当前数据库的版本
     */
    fun selectDbVersion(): String


}