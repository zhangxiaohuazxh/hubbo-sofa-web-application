## Dev

### 踩坑记录

1. Maven编译时不能正确识别Kotlin编写的注解
2. Kotlin no-arg插件为data class生成的无参构造函数,在Idea中无法被正确识别,使用到无参构造的地方语法都会报错,
   但是可正常运行,建议给所有的参数添加默认值,让Kotlin自己生成无参构造函数

## Ops

### 踩坑记录

### 待解决问题

1. sofa boot 程序打包ark jar包程序运行失败，报错 no class found
2. 支持native image
