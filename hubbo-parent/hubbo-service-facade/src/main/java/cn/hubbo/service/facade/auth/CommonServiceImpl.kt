package cn.hubbo.service.facade.auth

import cn.hubbo.dal.auth.DictDataMapper
import cn.hubbo.service.auth.CommonService
import org.springframework.stereotype.Service

@Service
class CommonServiceImpl(
    val dictDataMapper: DictDataMapper? = null
) : CommonService {


    override fun dbVersion(): String {
        return dictDataMapper!!.selectDbVersion()
    }


}