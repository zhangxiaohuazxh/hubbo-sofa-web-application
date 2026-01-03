package cn.hubbo.utils.coding

import com.google.common.collect.ImmutableList
import java.lang.reflect.Type
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeMirror

data class MethodDeclaration(
    /**
     * 函数名
     */
    val methodName: String,

    /**
     * 返回值类型
     */
    val returnType: TypeMirror?,

    /**
     * 修饰符
     */
    val modifiers: Set<Modifier>,

    /**
     * 参数列表
     */
    val parameters: List<ParameterDeclaration>?
) {

}
