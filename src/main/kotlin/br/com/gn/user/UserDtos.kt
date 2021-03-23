package br.com.gn.user

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@Introspected
data class NewUserRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val name: String
) {
    fun toModel(): User {
        return User(
            email = email,
            name = name
        )
    }
}

@Introspected
data class UpdateUserRequest(
    @field:NotBlank @field:Email val email: String
)