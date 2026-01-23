package cn.hubbo.assist.domain

import com.alibaba.fastjson2.annotation.JSONCompiled
import com.alibaba.fastjson2.annotation.JSONType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@JSONType
@JSONCompiled
data class User(@Json(name = "username") val username: String) {

}