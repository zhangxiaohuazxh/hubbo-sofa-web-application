package cn.hubbo.utils

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

object YamlUtils {

    val yaml: Yaml = Yaml()

    @JvmStatic
    inline fun <reified T> parse(inputStream: InputStream): T =
        inputStream.use { yaml.load<T>(it) } as T

}