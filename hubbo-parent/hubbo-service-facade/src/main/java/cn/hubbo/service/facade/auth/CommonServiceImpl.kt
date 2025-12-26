package cn.hubbo.service.facade.auth

import cn.hubbo.dal.auth.DictDataMapper
import cn.hubbo.service.auth.CommonService
import com.alipay.sofa.runtime.api.annotation.SofaService
import org.springframework.stereotype.Service

@SofaService(interfaceType = CommonService::class)
@Service
class CommonServiceImpl(
    val dictDataMapper: DictDataMapper? = null
) : CommonService {


    override fun dbVersion(): String {
        return dictDataMapper!!.selectDbVersion()
    }


}