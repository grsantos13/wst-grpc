package br.com.gn.deliveryplace

import br.com.gn.DeleteDeliveryPlaceRequest
import br.com.gn.DeliveryPlaceResponse
import br.com.gn.DeliveryPlaceServiceGrpc
import br.com.gn.DeliveryPlacesResponse
import br.com.gn.NewDeliveryPlaceRequest
import br.com.gn.ReadDeliveryPlaceRequest
import br.com.gn.shared.exception.ErrorHandler
import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeliveryPlaceEndpoint(
    private val repository: DeliveryPlaceRepository
) : DeliveryPlaceServiceGrpc.DeliveryPlaceServiceImplBase() {

    override fun create(request: NewDeliveryPlaceRequest, responseObserver: StreamObserver<DeliveryPlaceResponse>) {
        if (request.name.isNullOrBlank())
            throw IllegalArgumentException("Name must be informed")

        if (repository.existsByName(request.name))
            throw ObjectAlreadyExistsException("Delivery place already exists with name ${request.name}")

        val deliveryPlace = repository.save(request.toModel())
        val response = grpcDeliveryPlaceResponse(deliveryPlace)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


    override fun read(request: ReadDeliveryPlaceRequest, responseObserver: StreamObserver<DeliveryPlacesResponse>) {
        val responseList = repository.findAll()
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
        val deliveryPlace = repository.findById(UUID.fromString(request.id))
            .orElseThrow { ObjectNotFoundException("Delivery place not found with id ${request.id}") }

        repository.delete(deliveryPlace)

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
