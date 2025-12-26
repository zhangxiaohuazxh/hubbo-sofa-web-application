package cn.hubbo.entity.vo

data class UserLoginParams(
    val userId: String? = null,
    val passwd: String? = null,
    val tenantId: Long? = null,
    val sourceIP: String? = null
) {


}
