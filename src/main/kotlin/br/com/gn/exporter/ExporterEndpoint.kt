package br.com.gn.exporter

import br.com.gn.DeleteExporterRequest
import br.com.gn.ExporterResponse
import br.com.gn.ExporterServiceGrpc
import br.com.gn.ExportersResponse
import br.com.gn.Incoterm
import br.com.gn.Currency
import br.com.gn.NewExporterRequest
import br.com.gn.PaymentTerms
import br.com.gn.ReadExporterRequest
import br.com.gn.UpdateExporterRequest
import br.com.gn.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.transaction.Transactional

@ErrorHandler
@Singleton
class ExporterEndpoint(
    private val service: ExporterService
) : ExporterServiceGrpc.ExporterServiceImplBase() {

    @Transactional
    override fun create(request: NewExporterRequest, responseObserver: StreamObserver<ExporterResponse>) {
        val exporter = service.create(request = request.toRequestModel())
        val response = grpcExporterResponse(exporter)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


    @Transactional
    override fun read(request: ReadExporterRequest, responseObserver: StreamObserver<ExportersResponse>) {
        val responseList = service.read(request.name)
            .map { grpcExporterResponse(it) }

        val response = ExportersResponse.newBuilder()
            .addAllExporters(responseList)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @Transactional
    override fun update(request: UpdateExporterRequest, responseObserver: StreamObserver<ExporterResponse>) {
        val exporter = service.update(request = request.toRequestModel(), id = request.id)
        val response = grpcExporterResponse(exporter)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @Transactional
    override fun delete(request: DeleteExporterRequest, responseObserver: StreamObserver<ExporterResponse>) {
        val exporter = service.delete(request.id)
        val response = grpcExporterResponse(exporter)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun grpcExporterResponse(exporter: Exporter) = ExporterResponse.newBuilder()
        .setId(exporter.id.toString())
        .setCode(exporter.code)
        .setName(exporter.name)
        .setPaymentTerms(PaymentTerms.valueOf(exporter.paymentTerms.name))
        .setAddress(exporter.address.toGrpcAddress())
        .setCurrency(Currency.valueOf(exporter.currency.name))
        .setIncoterm(Incoterm.valueOf(exporter.incoterm.name))
        .build()
}
