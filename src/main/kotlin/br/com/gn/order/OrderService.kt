package br.com.gn.order

import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import br.com.gn.shared.validation.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Validated
@Singleton
class OrderService(
    private val repository: OrderRepository,
    private val manager: EntityManager
) {
    @Transactional
    fun create(@Valid request: NewOrderRequest): Order {
        if (repository.existsByNumber(request.number))
            throw ObjectAlreadyExistsException("Order with number ${request.number} already exists")

        val order = request.toModel(manager)
        repository.save(order)
        return order
    }

    @Transactional
    fun update(@Valid request: UpdateOrderRequest, @NotBlank @ValidUUID id: String): Order {
        return repository.findById(UUID.fromString(id))
            .orElseThrow { throw ObjectNotFoundException("Order not found with id $id") }
            .apply {
                val updateRequest = request.toUpdateRequest(manager)
                this.update(updateRequest)
            }

    }

    @Transactional
    fun updateObservation(@NotBlank observation: String, @NotBlank @ValidUUID id: String): Order {
        return repository.findById(UUID.fromString(id))
            .orElseThrow { throw ObjectNotFoundException("Order not found with id $id") }
            .apply {
                this.updateObservation(observation)
            }
    }

    @Transactional
    fun updateReference(@NotBlank @Size(max = 20) reference: String, @NotBlank @ValidUUID id: String): Order {
        if (repository.existsByBrokerReference(reference))
            throw ObjectAlreadyExistsException("Reference $reference already exists")

        return repository.findById(UUID.fromString(id))
            .orElseThrow { throw ObjectNotFoundException("Order not found with id $id") }
            .apply {
                this.updateReference(reference)
            }
    }

    fun delete(@NotBlank @ValidUUID id: String): Order {
        val order = repository.findById(UUID.fromString(id))
            .orElseThrow { throw ObjectNotFoundException("Order not found with id $id") }
        repository.delete(order)
        return order
    }

}