package br.com.gn.importer

import br.com.gn.DeleteImporterRequest
import br.com.gn.ImporterResponse
import br.com.gn.ImporterServiceGrpc
import br.com.gn.ImportersResponse
import br.com.gn.NewImporterRequest
import br.com.gn.ReadImporterRequest
import br.com.gn.UpdateImporterRequest
import br.com.gn.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.transaction.Transactional

@ErrorHandler
@Singleton
class ImporterEndpoint(
    private val service: ImporterService
) : ImporterServiceGrpc.ImporterServiceImplBase() {

    @Transactional
    override fun create(request: NewImporterRequest, responseObserver: StreamObserver<ImporterResponse>) {
        val importer: Importer = service.create(request.toRequestModel())

        val response = grpcImporterResponse(importer)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


    @Transactional
    override fun read(request: ReadImporterRequest, responseObserver: StreamObserver<ImportersResponse>) {
        val responseList = service.read(request.plant)
            .map { grpcImporterResponse(it) }

        val response = ImportersResponse.newBuilder()
            .addAllImporters(responseList)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @Transactional
    override fun update(request: UpdateImporterRequest, responseObserver: StreamObserver<ImporterResponse>) {
        val importer = service.update(request.toRequestModel(), request.id)
        val response = grpcImporterResponse(importer)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @Transactional
    override fun delete(request: DeleteImporterRequest, responseObserver: StreamObserver<ImporterResponse>) {
        val importer = service.delete(request.id)
        val response = grpcImporterResponse(importer)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun grpcImporterResponse(importer: Importer) = ImporterResponse.newBuilder()
        .setAddress(importer.address.toGrpcAddress())
        .setPlant(importer.plant)
        .setId(importer.id.toString())
        .build()
}
