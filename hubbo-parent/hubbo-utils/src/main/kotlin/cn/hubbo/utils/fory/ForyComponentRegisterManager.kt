package cn.hubbo.utils.fory

import cn.hubbo.common.annotation.Fory
import org.apache.fory.ThreadSafeFory
import java.io.File
import java.net.URL
import java.util.jar.JarInputStream

@Suppress("SpellCheckingInspection")
class ForyComponentRegisterManager {


    companion object {


        @Suppress("VariableNaming", "LocalVariableName")
        @JvmStatic
        fun register(fory: ThreadSafeFory) {
            val fory_entity_package_name = "cn.hubbo.entity"
            val classes = getClasses(fory_entity_package_name)
            classes.filter { it.isAnnotationPresent(Fory::class.java) }
                .forEach { fory.register(it) }
        }


        private fun getClasses(packageName: String): List<Class<*>> {
            val classes = mutableListOf<Class<*>>()
            val classLoader = Thread.currentThread().contextClassLoader
            val path = packageName.replace(".", "/")
            val resources = classLoader.getResources(path)
            while (resources.hasMoreElements()) {
                val resource = resources.nextElement()
                when (resource.protocol) {
                    "file" -> classes.addAll(findClassesInDirectory(File(resource.toURI()), packageName))
                    "jar" -> classes.addAll(findClassesInJar(resource, packageName))
                }
            }
            return classes
        }


        private fun findClassesInDirectory(directory: File, packageName: String): List<Class<*>> {
            val classes = mutableListOf<Class<*>>()
            if (!directory.exists()) {
                return classes
            }
            directory.listFiles()?.forEach { file ->
                when {
                    file.isDirectory -> {
                        classes.addAll(findClassesInDirectory(file, "$packageName.${file.name}"))
                    }

                    file.name.endsWith(".class") -> {
                        try {
                            val className = packageName + '.' + file.name.substring(0, file.name.length - 6)
                            classes.add(Class.forName(className))
                        } catch (e: ClassNotFoundException) {
                            throw ClassNotFoundException("Class not found: $file")
                        }
                    }
                }
            }
            return classes
        }


        private fun findClassesInJar(resource: URL, packageName: String): List<Class<*>> {
            val classes = mutableListOf<Class<*>>()
            val packagePath = packageName.replace('.', '/')
            try {
                JarInputStream(resource.openConnection().getInputStream()).use { jarStream ->
                    var entry = jarStream.nextJarEntry
                    while (entry != null) {
                        if (entry.name.startsWith(packagePath) && entry.name.endsWith(".class")) {
                            try {
                                val className = entry.name.substring(0, entry.name.length - 6).replace('/', '.')
                                classes.add(Class.forName(className))
                            } catch (e: ClassNotFoundException) {
                                // 忽略无法加载的类
                            }
                        }
                        entry = jarStream.nextJarEntry
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return classes
        }

    }



}