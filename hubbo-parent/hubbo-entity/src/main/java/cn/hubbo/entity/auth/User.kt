package cn.hubbo.entity.auth

import cn.hubbo.utils.fory.Fory
import com.mybatisflex.annotation.Id
import com.mybatisflex.annotation.Table

@Fory
@Table("t_user")
data class User(
    @Id
    val id: Long,
    val username: String
) {
}

