package br.com.gn.order.read

import br.com.gn.OrdersResponse
import br.com.gn.ReadOrderRequest
import br.com.gn.ReadOrderServiceGrpc
import br.com.gn.order.OrderRepository
import br.com.gn.order.toFilter
import br.com.gn.shared.exception.ErrorHandler
import br.com.gn.utils.toPageable
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ReadOrderEndpoint(
    private val repository: OrderRepository,
    private val validator: Validator
) : ReadOrderServiceGrpc.ReadOrderServiceImplBase() {

    override fun read(request: ReadOrderRequest, responseObserver: StreamObserver<OrdersResponse>) {
        val filter = request.toFilter(validator)
        val list = filter.filter(repository, request.pageable.toPageable())
            .map { it.toGrpcOrderResponse() }

        responseObserver.onNext(
            OrdersResponse.newBuilder()
                .addAllOrders(list)
                .setTotalPages(list.totalPages)
                .setTotalSize(list.totalSize.toInt())
                .setNumberOfElements(list.numberOfElements)
                .build()
        )
        responseObserver.onCompleted()
    }
}
