package br.com.gn.importer

import br.com.gn.*
import br.com.gn.NewImporterRequest
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
        responseObserver.onNext(importer.toGrpcImporterResponse())
        responseObserver.onCompleted()
    }


    @Transactional
    override fun read(request: ReadImporterRequest, responseObserver: StreamObserver<ImportersResponse>) {
        val responseList = service.read(request.plant)
            .map { it.toGrpcImporterResponse() }

        val response = ImportersResponse.newBuilder()
            .addAllImporters(responseList)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @Transactional
    override fun update(request: UpdateImporterRequest, responseObserver: StreamObserver<ImporterResponse>) {
        val importer = service.update(request.toRequestModel(), request.id)
        responseObserver.onNext(importer.toGrpcImporterResponse())
        responseObserver.onCompleted()
    }

    @Transactional
    override fun delete(request: DeleteImporterRequest, responseObserver: StreamObserver<ImporterResponse>) {
        val importer = service.delete(request.id)
        responseObserver.onNext(importer.toGrpcImporterResponse())
        responseObserver.onCompleted()
    }
}
