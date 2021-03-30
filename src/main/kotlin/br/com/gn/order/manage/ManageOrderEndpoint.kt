package br.com.gn.order.manage

import br.com.gn.ManageOrderServiceGrpc
import br.com.gn.NewOrderRequest
import br.com.gn.OrderResponse
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

}