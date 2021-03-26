package br.com.gn.order

import br.com.gn.shared.exception.ObjectAlreadyExistsException
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.Valid

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
}