package br.com.gn.process

import br.com.gn.operation.Operation
import br.com.gn.shared.exception.ObjectNotFoundException
import br.com.gn.shared.validation.ExistsResource
import br.com.gn.user.User
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.persistence.EntityManager
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class NewProcessRequest(
    @field:NotNull val responsibleId: UUID,
    @field:NotBlank val name: String,
    @field:NotNull val operationId: UUID
) {
    fun toModel(manager: EntityManager): Process {
        val responsible = manager.find(User::class.java, responsibleId)
            ?: throw ObjectNotFoundException("Responsible not found with id $responsibleId")
        val operation = manager.find(Operation::class.java, operationId)
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
    @field:NotNull @ExistsResource(field = "id", domainClass = User::class) val responsibleId: UUID
) {
    fun responsible(manager: EntityManager): User {
        return manager.find(User::class.java, responsibleId)
            ?: throw ObjectNotFoundException("Responsible not found with id $responsibleId")
    }
}
