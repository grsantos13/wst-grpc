package br.com.gn.process

import br.com.gn.operation.Operation
import br.com.gn.shared.exception.ObjectNotFoundException
import br.com.gn.shared.validation.ValidUUID
import br.com.gn.user.User
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.persistence.EntityManager
import javax.validation.constraints.NotBlank

@Introspected
data class NewProcessRequest(
    @field:NotBlank @field:ValidUUID val responsibleId: String,
    @field:NotBlank val name: String,
    @field:NotBlank @field:ValidUUID val operationId: String
) {
    fun toModel(manager: EntityManager): Process {
        val responsible = manager.find(User::class.java, UUID.fromString(responsibleId))
            ?: throw ObjectNotFoundException("Responsible not found with id $responsibleId")
        val operation = manager.find(Operation::class.java, UUID.fromString(operationId))
            ?: throw ObjectNotFoundException("Operation not found with id $operationId")

        return Process(
            responsible = responsible,
            name = name,
            operation = operation
        )
    }
}

@Introspected
data class UpdateProcessRequest(
    @field:NotBlank @field:ValidUUID val responsibleId: String
) {
    fun responsible(manager: EntityManager): User {
        return manager.find(User::class.java, UUID.fromString(responsibleId))
            ?: throw ObjectNotFoundException("Responsible not found with id $responsibleId")
    }
}
