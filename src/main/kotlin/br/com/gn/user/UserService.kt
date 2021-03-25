package br.com.gn.user

import br.com.gn.ReadUserRequest
import br.com.gn.ReadUserRequest.ConsultaCase.EMAIL
import br.com.gn.ReadUserRequest.ConsultaCase.NAME
import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import br.com.gn.shared.validation.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class UserService(
    private val repository: UserRepository
) {

    @Transactional
    fun create(@Valid request: NewUserRequest): User {
        val existsByEmail = repository.existsByEmail(request.email)
        if (existsByEmail)
            throw ObjectAlreadyExistsException("User already exists with email ${request.email}")

        val user = request.toModel()
        repository.save(user)
        return user
    }

    @Transactional
    fun read(request: ReadUserRequest): List<User> {
        return when (request.consultaCase) {
            EMAIL -> {
                if (request.email.isNullOrBlank())
                    throw IllegalArgumentException("Email must be informed for a filter")
                repository.findByEmail(request.email)
            }
            NAME -> {
                if (request.name.isNullOrBlank())
                    throw IllegalArgumentException("Name must be informed for a filter")
                repository.findByName(request.name)
            }
            else -> repository.findAll()
        }
    }

    @Transactional
    fun update(@Valid request: UpdateUserRequest, @NotBlank @ValidUUID id: String): User {
        val existsByEmail = repository.existsByEmail(request.email)
        if (existsByEmail)
            throw ObjectAlreadyExistsException("User already exists with email ${request.email}")

        val user = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("User not found with id $id") }

        user.update(request.email)
        return user
    }

    @Transactional
    fun delete(@NotBlank @ValidUUID id: String): User {
        val user = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("User not found with id $id") }

        repository.delete(user)
        return user
    }


}
