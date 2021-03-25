package br.com.gn.process

import br.com.gn.DeleteProcessRequest
import br.com.gn.NewProcessRequest
import br.com.gn.OperationType
import br.com.gn.ProcessResponse
import br.com.gn.ProcessServiceGrpc
import br.com.gn.ProcessesResponse
import br.com.gn.ReadProcessRequest
import br.com.gn.UpdateProcessRequest
import br.com.gn.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ProcessEndpoint(
    private val service: ProcessService
) : ProcessServiceGrpc.ProcessServiceImplBase() {

    override fun create(request: NewProcessRequest, responseObserver: StreamObserver<ProcessResponse>) {
        val processRequest = request.toRequestModel()
        val process = service.create(processRequest)
        val response = grpcProcessResponse(process)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun read(request: ReadProcessRequest, responseObserver: StreamObserver<ProcessesResponse>) {
        val list = service.read(request.name)
            .map { grpcProcessResponse(it) }

        val response = ProcessesResponse.newBuilder()
            .addAllProcesses(list)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun update(request: UpdateProcessRequest, responseObserver: StreamObserver<ProcessResponse>) {
        val processRequest = request.toRequestModel()
        val process = service.update(processRequest, request.id)
        val response = grpcProcessResponse(process)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun delete(request: DeleteProcessRequest, responseObserver: StreamObserver<ProcessResponse>) {
        val process = service.delete(request.id)
        val response = grpcProcessResponse(process)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun grpcProcessResponse(process: Process) = ProcessResponse.newBuilder()
        .setCountry(process.operation.country)
        .setType(OperationType.valueOf(process.operation.type.name))
        .setId(process.id.toString())
        .setName(process.name)
        .setResponsible(process.responsible.name)
        .build()
}