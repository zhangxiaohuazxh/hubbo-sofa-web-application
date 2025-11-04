package cn.hubbo.service.auth

import cn.hubbo.dal.auth.DictDataMapper
import com.alipay.sofa.runtime.api.annotation.SofaService
import lombok.AllArgsConstructor
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