package br.com.gn.order.manage

import br.com.gn.*
import br.com.gn.order.OrderService
import br.com.gn.order.toRequestModel
import br.com.gn.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ManageOrderEndpoint(
    private val service: OrderService,
) : ManageOrderServiceGrpc.ManageOrderServiceImplBase() {

    override fun create(request: NewOrderRequest, responseObserver: StreamObserver<OrderResponse>) {
        val orderRequest = request.toRequestModel()
        val order = service.create(orderRequest)
        responseObserver.onNext(order.toGrpcOrderResponse())
        responseObserver.onCompleted()
    }

    override fun update(request: UpdateOrderRequest, responseObserver: StreamObserver<OrderResponse>) {
        val orderRequest = request.toRequestModel()
        val order = service.update(orderRequest, request.id)
        responseObserver.onNext(order.toGrpcOrderResponse())
        responseObserver.onCompleted()
    }

    override fun updateObs(request: UpdateObsOrderRequest, responseObserver: StreamObserver<OrderResponse>) {
        val order = service.updateObservation(request.observation, request.id)
        responseObserver.onNext(order.toGrpcOrderResponse())
        responseObserver.onCompleted()
    }

    override fun updateRef(request: UpdateRefOrderRequest, responseObserver: StreamObserver<OrderResponse>) {
        val order = service.updateReference(request.reference, request.id)
        responseObserver.onNext(order.toGrpcOrderResponse())
        responseObserver.onCompleted()
    }

    override fun delete(request: DeleteOrderRequest, responseObserver: StreamObserver<OrderResponse>) {
        val order = service.delete(request.id)
        responseObserver.onNext(order.toGrpcOrderResponse())
        responseObserver.onCompleted()
    }
}