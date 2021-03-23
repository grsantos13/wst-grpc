package br.com.gn.user

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "user_email_uk", columnNames = ["email"])
    ]
)
class User(
    @NotBlank @Email email: String,
    @field:NotBlank @Column(nullable = false) val name: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

    @field:NotBlank
    @field:Email
    @Column(nullable = false)
    var email = email
        private set

    fun update(@NotBlank @Email email: String) {
        this.email = email
    }
}