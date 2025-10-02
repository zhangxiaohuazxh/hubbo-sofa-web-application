package cn.hubbo.entity.auth

import com.mybatisflex.annotation.Id
import com.mybatisflex.annotation.Table


@Table("t_user")
data class User(
    @Id
    val id: Long,
    val username: String
) {
}

