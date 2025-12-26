package cn.hubbo.entity.vo

data class ResultVO<T>(val code: Int? = 200, val msg: String? = "success", val data: T? = null) {


    companion object {

        @JvmStatic
        fun <T> success(): ResultVO<T> {
            return ResultVO(0, "success", null)
        }

        @JvmStatic
        fun <T> success(data: T): ResultVO<T> {
            return ResultVO(0, "success", data)
        }

    }


}
