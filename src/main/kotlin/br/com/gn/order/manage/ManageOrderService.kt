package br.com.gn.order.manage

import br.com.gn.NotifyRouteRequest
import br.com.gn.OperationType
import br.com.gn.RouteServiceGrpc
import br.com.gn.order.NewOrderRequest
import br.com.gn.order.Order
import br.com.gn.order.OrderRepository
import br.com.gn.order.UpdateOrderRequest
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
class ManageOrderService(
    private val repository: OrderRepository,
    private val manager: EntityManager,
    private val wstRouteClient: RouteServiceGrpc.RouteServiceBlockingStub
) {
    @Transactional
    fun create(@Valid request: NewOrderRequest): Order {
        if (repository.existsByNumber(request.number))
            throw ObjectAlreadyExistsException("Order with number ${request.number} already exists")

        val order = request.toModel(manager)
        notifyRoute(order)

        repository.save(order)
        return order
    }

    @Transactional
    fun update(@Valid request: UpdateOrderRequest, @NotBlank @ValidUUID id: String): Order {
        return repository.findById(UUID.fromString(id))
            .orElseThrow { throw ObjectNotFoundException("Order not found with id $id") }
            .apply {
                notifyRoute(this)
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

    private fun notifyRoute(order: Order) {
        if (!order.route.isNullOrBlank())
            wstRouteClient.notify(
                NotifyRouteRequest.newBuilder()
                    .setExporterCode(order.exporter.code)
                    .setImporterPlant(order.importer.plant)
                    .setName(order.route)
                    .setType(OperationType.IMPORT)
                    .build()
            )
    }
}