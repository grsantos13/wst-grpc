package br.com.gn.deliveryplace

import br.com.gn.DeleteDeliveryPlaceRequest
import br.com.gn.DeliveryPlaceResponse
import br.com.gn.DeliveryPlaceServiceGrpc
import br.com.gn.DeliveryPlacesResponse
import br.com.gn.NewDeliveryPlaceRequest
import br.com.gn.ReadDeliveryPlaceRequest
import br.com.gn.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeliveryPlaceEndpoint(
    private val service: DeliveryPlaceService
) : DeliveryPlaceServiceGrpc.DeliveryPlaceServiceImplBase() {

    override fun create(request: NewDeliveryPlaceRequest, responseObserver: StreamObserver<DeliveryPlaceResponse>) {
        val deliveryPlace: DeliveryPlace = service.create(request.toRequestModel())
        val response = grpcDeliveryPlaceResponse(deliveryPlace)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


    override fun read(request: ReadDeliveryPlaceRequest, responseObserver: StreamObserver<DeliveryPlacesResponse>) {
        val responseList = service.read()
            .map { grpcDeliveryPlaceResponse(it) }

        val response = DeliveryPlacesResponse.newBuilder()
            .addAllPlaces(responseList)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun delete(
        request: DeleteDeliveryPlaceRequest,
        responseObserver: StreamObserver<DeliveryPlaceResponse>
    ) {
        val deliveryPlace: DeliveryPlace = service.delete(request.id)
        val response = grpcDeliveryPlaceResponse(deliveryPlace)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun grpcDeliveryPlaceResponse(deliveryPlace: DeliveryPlace) =
        DeliveryPlaceResponse.newBuilder()
            .setId(deliveryPlace.id.toString())
            .setName(deliveryPlace.name)
            .build()
}
