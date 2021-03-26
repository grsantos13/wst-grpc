package br.com.gn.order

import br.com.gn.Modal
import br.com.gn.NewOrderRequest
import br.com.gn.OrderResponse
import br.com.gn.OrderResponse.EventResponse
import br.com.gn.OrderServiceGrpc
import br.com.gn.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class OrderEndpoint(
    private val service: OrderService
) : OrderServiceGrpc.OrderServiceImplBase() {

    override fun create(request: NewOrderRequest, responseObserver: StreamObserver<OrderResponse>) {
        val orderRequest = request.toRequestModel()
        val order = service.create(orderRequest)
        val response = grpcOrderResponse(order)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun grpcOrderResponse(order: Order): OrderResponse {
        with(order) {
            return OrderResponse.newBuilder()
                .setOrigin(origin)
                .setDestination(destination)
                .setExporter(exporter.name)
                .addAllItems(items!!.map {
                    OrderResponse.ItemResponse.newBuilder()
                        .setId(it.id.toString())
                        .setCode(it.material.code).setDescription(it.material.description)
                        .setQuantity(it.quantity.toString())
                        .build()
                })
                .setNumber(number)
                .setImporter(importer.plant)
                .setDate(date.toString())
                .setResponsible(responsible.name)
                .setModal(Modal.valueOf(modal.name))
                .setNecessity(necessity.toString())
                .setDeadline(deadline.toString())
                .setObservation(observation ?: "")
                .setDeliveryPlace(deliveryPlace?.name ?: "")
                .setId(id.toString())
                .setEvents(
                    EventResponse.newBuilder()
                        .setAvailability(event.availability.toString())
                        .setEstimatedDeparture(event.estimatedDeparture.toString())
                        .setRealDeparture(event.realDeparture.toString())
                        .setEstimatedArrival(event.estimatedArrival.toString())
                        .setRealArrival(event.realArrival.toString())
                        .setPreAlert(event.preAlert.toString())
                        .setWrongNecessityAlert(event.wrongNecessityAlert.toString())
                        .setId(event.id.toString())
                        .build()
                )
                .build()
        }
    }
}