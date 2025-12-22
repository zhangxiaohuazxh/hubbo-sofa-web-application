package cn.hubbo.utils.benchmark

class StopWatch {

    /* 任务名称 */
    var name: String? = null

    /* 开始时间 */
    var start: Long? = null

    /* 是否运行中 */
    var running: Boolean? = false

    constructor()

    constructor(name: String) {
        this.name = name
    }

    fun start(): StopWatch {
        this.start = System.nanoTime()
        return this
    }

    fun stop(): StopWatch {
        val end = System.nanoTime()
        return this
    }

    fun reset(): StopWatch {
        this.start = null
        this.running = false
        return this
    }


}

