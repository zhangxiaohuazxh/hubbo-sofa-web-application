package cn.hubbo.utils.coding

import com.google.common.collect.ImmutableList
import java.lang.reflect.Type
import javax.lang.model.type.TypeMirror

data class ParameterDeclaration(
    /**
     * 参数名称
     */
    val parameterName: String,

    /**
     * 参数类型
     */
    val parameterType: TypeMirror,

    /**
     * 参数注解
     */
    val annotations: ImmutableList<AnnotationDeclaration>?
) {


}
