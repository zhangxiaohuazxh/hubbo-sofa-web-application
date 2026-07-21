package cn.hubbo.utils

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

class YamlUtils {

    companion object {

        @JvmStatic
        inline fun <reified T> parse(inputstream: InputStream): T {
            val yaml = Yaml()
            val res = yaml.load<T>(inputstream)
            return res as T
        }

    }

}