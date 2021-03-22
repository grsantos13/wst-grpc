package br.com.gn.exporter

import br.com.gn.DeleteExporterRequest
import br.com.gn.ExporterResponse
import br.com.gn.ExporterServiceGrpc
import br.com.gn.ExportersResponse
import br.com.gn.Incoterm
import br.com.gn.NewExporterRequest
import br.com.gn.PaymentTerms
import br.com.gn.ReadExporterRequest
import br.com.gn.UpdateExporterRequest
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import java.util.*
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional

@Singleton
open class ExporterEndpoint(
    private val manager: EntityManager,
    private val validator: Validator
) : ExporterServiceGrpc.ExporterServiceImplBase() {

    @Transactional
    override fun create(request: NewExporterRequest, responseObserver: StreamObserver<ExporterResponse>) {
        val exporterRequest = request.toRequestModel()
        validator.validate(exporterRequest)

        val exporter = exporterRequest.toModel()
        manager.persist(exporter)

        val response = grpcExporterResponse(exporter)

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


    @Transactional
    override fun read(request: ReadExporterRequest, responseObserver: StreamObserver<ExportersResponse>) {
        var responseList: List<ExporterResponse>
        if (request.name.isNullOrBlank())
            responseList = manager.createQuery(" select e from Exporter e ", Exporter::class.java)
                .resultList
                .map { grpcExporterResponse(it) }
        else
            responseList = manager.createQuery(" select e from Exporter e where e.name = :name ", Exporter::class.java)
                .setParameter("name", request.name)
                .resultList
                .map { grpcExporterResponse(it) }

        val response = ExportersResponse.newBuilder()
            .addAllExporters(responseList)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @Transactional
    override fun update(request: UpdateExporterRequest, responseObserver: StreamObserver<ExporterResponse>) {
        val exporter = manager.find(Exporter::class.java, UUID.fromString(request.id))
            ?: throw IllegalArgumentException("Exporter not found with id ${request.id}")

        val exporterRequest = request.toRequestModel()
        validator.validate(exporterRequest)

        exporter.update(exporterRequest)

        val response = grpcExporterResponse(exporter)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @Transactional
    override fun delete(request: DeleteExporterRequest, responseObserver: StreamObserver<ExporterResponse>) {
        val exporter = manager.find(Exporter::class.java, UUID.fromString(request.id))
            ?: throw IllegalArgumentException("Exporter not found with id ${request.id}")

        manager.remove(exporter)

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
        .setIncoterm(Incoterm.valueOf(exporter.incoterm.name))
        .build()
}
