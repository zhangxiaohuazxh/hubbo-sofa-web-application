package cn.hubbo.entity.vo

data class ResultVO<E>(val code: Int? = 200, val msg: String? = "success", val data: E? = null) {


    companion object {

        @JvmStatic
        fun success(): ResultVO<Any> {
            return ResultVO(0, "success", null)
        }

        @JvmStatic
        fun success(data: Any): ResultVO<Any> {
            return ResultVO(0, "success", data)
        }

    }


}
