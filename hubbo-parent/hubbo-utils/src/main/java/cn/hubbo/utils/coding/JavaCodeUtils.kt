package cn.hubbo.utils.coding

import com.google.common.collect.Lists.newArrayList
import com.squareup.javapoet.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Predicate
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Poet使用说明
 * <br/>
 *
 * <font color="blue"><a href="https://juejin.cn/post/6844903456629587976">参考文档</a></font>
 *
 * -. 占位符
 * 1. $L 字面量 如 666 "abc"
 * 2. $S 字符串 如 "abc"
 * 3. $N 引用前文中定义的变量
 * 4. $T 引用类，如 String.class，poet会自动引包
 *
 * 二. 常用类
 * 1. JavaFile 代表一个Java源文件
 * 2. TypeSpec 代表类、接口...的定义
 * 3. FieldSpec 类中变量的定义，可以是静态变量、实例变量、常量
 * 4. MethodSpec 类方法定义，可以是静态方法、实例方法
 * 5. ParameterSpec 方法参数定义
 * 6. AnnotationSpec 注解定义
 * 7. ClassName 类名，如 String.class
 * 8. TypeName 类型名，如 TypeName.DOUBLE 可以是返回值类型，也可以是函数参数类型、全局变量的定义
 *
 * 三. tips
 * 1. String[] Type写法 String[].classs
 */

private val log: Logger = LoggerFactory.getLogger(JavaCodeUtils::class.java)

class JavaCodeUtils {


    companion object {

        val logger: Logger = LoggerFactory.getLogger(JavaCodeUtils::class.java)


        @JvmStatic
        fun getTypeSpecBuilder(className: String): TypeSpec.Builder {
            return TypeSpec.classBuilder(className)
        }

        @JvmStatic
        fun getMethodSpecBuilder(methodName: String): MethodSpec.Builder {
            return MethodSpec.methodBuilder(methodName)
        }

        @JvmStatic
        fun writeSourceCode(javaFile: JavaFile?, filer: Filer) {
            javaFile?.writeTo(filer)
        }

        @JvmStatic
        fun createJavaTypeSpec(
            className: String, vararg modifiers: Modifier
        ): TypeSpec {
            return TypeSpec.classBuilder(className).addModifiers(*modifiers).build()
        }

        @JvmStatic
        fun createJavaTypeSpec(
            className: String, modifiers: MutableList<Modifier>, annotations: MutableList<AnnotationSpec>
        ): TypeSpec {
            return TypeSpec.classBuilder(className).addModifiers(*modifiers.toTypedArray<Modifier>())
                .addAnnotations(annotations)
                .build()
        }


        @JvmStatic
        fun createJavFile(packageName: String, type: TypeSpec, comment: String): JavaFile {
            return JavaFile.builder(packageName, type).addFileComment(comment).build()
        }

        @JvmStatic
        fun createJavFile(source: JavaSourceStructureInfo): JavaFile {
            return JavaFile.builder(source.packageName, generateSourceCode(source))
                .addFileComment(source.comment)
                .build()
        }

        @JvmStatic
        fun parseSourceCode(annotatedElement: TypeElement, roundEnv: RoundEnvironment): JavaSourceStructureInfo {
            return parseSourceCode(annotatedElement, roundEnv, null)
        }

        @JvmStatic
        fun parseSourceCode(
            annotatedElement: TypeElement, ignore: RoundEnvironment, predicate: Predicate<ExecutableElement>?
        ): JavaSourceStructureInfo {
            log.info("开始解析{}", annotatedElement.qualifiedName)
            val methods = newArrayList<MethodDeclaration>()
            val structureInfo = JavaSourceStructureInfo(
                modifiers = annotatedElement.modifiers,
                className = annotatedElement.simpleName.toString(),
                methodDeclarations = methods,
                supperClass = annotatedElement.asType(),
            )
            for (enclosedElement in annotatedElement.enclosedElements) {
                if (enclosedElement.kind == ElementKind.METHOD && enclosedElement is ExecutableElement) {
                    predicate?.let {
                        if (!it.test(enclosedElement)) {
                            continue
                        }
                    }
                    val parameterDeclarations = newArrayList<ParameterDeclaration>()
                    enclosedElement.parameters.let {
                        for (variableElement in it) {
                            parameterDeclarations.add(
                                ParameterDeclaration(
                                    parameterName = variableElement.simpleName.toString(),
                                    parameterType = variableElement.asType(),
                                    null
                                )
                            )
                        }
                    }
                    val methodDeclaration = MethodDeclaration(
                        methodName = enclosedElement.simpleName.toString(),
                        returnType = enclosedElement.returnType,
                        parameters = parameterDeclarations,
                        modifiers = enclosedElement.modifiers.filter { modifier -> modifier != Modifier.NATIVE }
                            .toHashSet()
                    )
                    methods.add(methodDeclaration)
                }
            }
            log.info("{}解析结束", annotatedElement.qualifiedName)
            return structureInfo
        }

        fun generateSourceCode(sourceInfo: JavaSourceStructureInfo): TypeSpec {
            log.info("开始生成{}的源码", sourceInfo.className)
            val typeSpecBuilder = getTypeSpecBuilder(sourceInfo.subClassName())
                .addModifiers(*sourceInfo.modifiers.toTypedArray<Modifier>())
                .superclass(TypeName.get(sourceInfo.supperClass))
                .addStaticBlock(CodeBlock.builder().build())
            sourceInfo.methodDeclarations?.let {
                for (declaration in it) {
                    typeSpecBuilder.addMethod(generateSingleMethod(declaration))
                }
            }
            log.info("生成{}的源码成功", sourceInfo.className)
            return typeSpecBuilder.build()
        }

        private fun mapTypeName(typeString: String): TypeName {
            return when (typeString) {
                "int" -> TypeName.INT
                "long" -> TypeName.LONG
                "float" -> TypeName.FLOAT
                "double" -> TypeName.DOUBLE
                "boolean" -> TypeName.BOOLEAN
                "byte" -> TypeName.BYTE
                "short" -> TypeName.SHORT
                "char" -> TypeName.CHAR
                "void" -> TypeName.VOID
                "java.lang.String" -> TypeName.get(String::class.java)
                else -> {
                    // 尝试处理数组类型
                    if (typeString.endsWith("[]")) {
                        val componentType = typeString.substring(0, typeString.length - 2)
                        val componentTypeName = mapTypeName(componentType)
                        return ArrayTypeName.of(componentTypeName)
                    }
                    TODO("unsported type: $typeString")
                }
            }
        }

        private fun generateSingleMethod(declaration: MethodDeclaration): MethodSpec {
            //  处理函数签名
            val methodBuilder = MethodSpec.methodBuilder(declaration.methodName)
                .addModifiers(declaration.modifiers)
                .addAnnotation(Override::class.java)
            // 处理函数形参
            declaration.parameters?.forEach { param ->
                methodBuilder.addParameter(
                    ParameterSpec.builder(TypeName.get(param.parameterType), param.parameterName).build()
                )
            }
            // 处理返回值类型
            declaration.returnType?.let { returnType ->
                methodBuilder.returns(TypeName.get(returnType))
            } ?: run {
                methodBuilder.returns(TypeName.VOID)
            }
            // 根据返回类型添加默认的函数体
            val returnTypeString = declaration.returnType?.toString() ?: "void"
            if (returnTypeString == "void" || returnTypeString.endsWith(".Void")) {
                methodBuilder.addCode("")
            } else {
                // 根据返回类型添加默认返回值
                when (returnTypeString) {
                    "boolean", "java.lang.Boolean" -> methodBuilder.addStatement("return false")
                    "byte", "java.lang.Byte" -> methodBuilder.addStatement("return 0")
                    "short", "java.lang.Short" -> methodBuilder.addStatement("return 0")
                    "int", "java.lang.Integer" -> methodBuilder.addStatement("return 0")
                    "long", "java.lang.Long" -> methodBuilder.addStatement("return 0L")
                    "float", "java.lang.Float" -> methodBuilder.addStatement("return 0.0f")
                    "double", "java.lang.Double" -> methodBuilder.addStatement("return 0.0d")
                    "char", "java.lang.Character" -> methodBuilder.addStatement("return 0")
                    else -> methodBuilder.addStatement("return null")
                }
            }
            return methodBuilder.build()
        }


    }


}

