package br.com.gn.operation

import br.com.gn.DeleteOperationRequest
import br.com.gn.NewOperationRequest
import br.com.gn.OperationResponse
import br.com.gn.OperationServiceGrpc
import br.com.gn.OperationType.valueOf
import br.com.gn.OperationsResponse
import br.com.gn.ReadOperationRequest
import br.com.gn.shared.exception.ErrorHandler
import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional

@ErrorHandler
@Singleton
class OperationEndpoint(
    private val repository: OperationRepository
) : OperationServiceGrpc.OperationServiceImplBase() {

    @Transactional
    override fun create(request: NewOperationRequest, responseObserver: StreamObserver<OperationResponse>) {
        if (request.country.isNullOrBlank())
            throw IllegalArgumentException("Country must not be blank")

        if (request.type.name == "UNKNOWN_OPERATION")
            throw IllegalArgumentException("Type must not be blank")

        val exists = repository.existsByCountryAndType(
            request.country,
            OperationType.valueOf(request.type.name)
        )

        if (exists)
            throw ObjectAlreadyExistsException("Operation with country ${request.country} and type ${request.type} already exists")

        val operation = request.toModel()
        repository.save(operation)

        responseObserver.onNext(
            OperationResponse.newBuilder()
                .setId(operation.id.toString())
                .setCountry(operation.country)
                .setType(valueOf(operation.type.name))
                .build()
        )
        responseObserver.onCompleted()

    }

    @Transactional
    override fun read(request: ReadOperationRequest, responseObserver: StreamObserver<OperationsResponse>) {
        val responseList = repository.findAll()
            .map {
                OperationResponse.newBuilder()
                    .setId(it.id.toString())
                    .setCountry(it.country)
                    .setType(valueOf(it.type.name))
                    .build()
            }

        val response = OperationsResponse.newBuilder()
            .addAllOperations(responseList)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @Transactional
    override fun delete(request: DeleteOperationRequest, responseObserver: StreamObserver<OperationResponse>) {
        if (request.id.isNullOrBlank())
            throw IllegalArgumentException("Id must not be blank")

        val operation = repository.findById(UUID.fromString(request.id))
            .orElseThrow { throw ObjectNotFoundException("Operation not found with id ${request.id}") }

        repository.delete(operation)
        responseObserver.onNext(
            OperationResponse.newBuilder()
                .setId(operation.id.toString())
                .setCountry(operation.country)
                .setType(valueOf(operation.type.name))
                .build()
        )
        responseObserver.onCompleted()
    }
}


