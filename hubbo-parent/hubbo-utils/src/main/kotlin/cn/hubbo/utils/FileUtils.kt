package cn.hubbo.utils

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.apache.commons.lang3.StringUtils
import java.io.File

object FileUtils {

    private val mapping: Map<String, List<String>> = hashMapOf(
        "java" to listOf("pom.xml", "build.gradle.kts"),
        "rust" to listOf("Cargo.toml"),
        "node" to listOf("packages.json"),
    )

    @JvmStatic
    suspend fun markProjectStructure(directory: File, type: String): Collection<File> {
        val namedFilter = NameFileFilter(mapping[type.lowercase()] ?: return emptyList())
        return FileUtils.listFiles(directory, namedFilter, TrueFileFilter.INSTANCE).distinct()
    }

    suspend fun markProjectStructureRelativePath(directory: File, type: String): Collection<String> {
        return markProjectStructure(directory, type)
            .map { File(it.parent) }
            .map { it.relativeTo(directory).toString() }
            .filter { StringUtils.isNotBlank(it) }
    }

    /** 把相对路径结果改写成目录树：父目录作为父节点 */
    suspend fun markProjectStructureTree(directory: File, type: String): DirNode {
        val root = DirNode(directory.name)
        markProjectStructureRelativePath(directory, type).forEach(root::add)
        return root
    }

}

/** 目录树节点，name 为目录名，children 为子目录节点 */
data class DirNode(
    val name: String,
    val children: MutableMap<String, DirNode> = LinkedHashMap(),
) {

    /** 将一段相对路径按 "/" 或 "\" 拆分后挂到树上 */
    fun add(path: String) {
        if (StringUtils.isBlank(path)) return
        path.split('/', '\\')
            .filter { it.isNotBlank() }
            .fold(this) { node, part -> node.children.getOrPut(part) { DirNode(part) } }
    }

    /** 渲染成文本树，用于日志展示 */
    fun render(): String = buildString {
        fun visit(node: DirNode, prefix: String, isLast: Boolean) {
            append(prefix)
            append(if (isLast) "└── " else "├── ")
            append(node.name)
            appendLine()
            val childPrefix = prefix + if (isLast) "    " else "│   "
            val kids = node.children.values.toList()
            kids.forEachIndexed { index, child -> visit(child, childPrefix, index == kids.size - 1) }
        }
        children.values.toList().forEachIndexed { index, child -> visit(child, "", index == children.size - 1) }
    }
}
