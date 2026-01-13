package cn.hubbo.utils.coding

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.foreign.Arena

@SuppressWarnings(value = ["all"])
class MemoryAllocator {


    companion object {

        private val log: Logger = LoggerFactory.getLogger(MemoryAllocator::class.java)

        private val allocator = Arena.ofConfined()

    }


}