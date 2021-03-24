package br.com.gn.material

import br.com.gn.DeleteMaterialRequest
import br.com.gn.MaterialResponse
import br.com.gn.MaterialServiceGrpc
import br.com.gn.MaterialsResponse
import br.com.gn.NewMaterialRequest
import br.com.gn.ReadMaterialRequest
import br.com.gn.UpdateMaterialRequest
import br.com.gn.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.data.model.Page
import javax.inject.Singleton

@ErrorHandler
@Singleton
class MaterialEndpoint(
    private val service: MaterialService
) : MaterialServiceGrpc.MaterialServiceImplBase() {

    override fun create(request: NewMaterialRequest, responseObserver: StreamObserver<MaterialResponse>) {
        val materialRequest = request.toRequestModel()
        val material = service.create(materialRequest)
        val response = grpcMaterialResponse(material)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun read(request: ReadMaterialRequest, responseObserver: StreamObserver<MaterialsResponse>) {
        val materialRequest = request.toRequestModel()
        val page = service.read(materialRequest)
        val response = grpcPageResponse(page)
        responseObserver.onNext(response)
        responseObserver.onCompleted()

    }

    private fun grpcPageResponse(page: Page<Material>): MaterialsResponse {
        val pageMaterialResponse = page.map { grpcMaterialResponse(it) }
        return MaterialsResponse.newBuilder()
            .addAllMaterials(pageMaterialResponse.content)
            .setTotalPages(pageMaterialResponse.totalPages.toLong())
            .setNumberOfElements(pageMaterialResponse.numberOfElements.toLong())
            .setTotalSize(pageMaterialResponse.totalSize.toLong())
            .build()
    }

    override fun update(request: UpdateMaterialRequest, responseObserver: StreamObserver<MaterialResponse>) {
        val materialRequest = request.toRequestModel()
        val material = service.update(materialRequest, request.id)
        val response = grpcMaterialResponse(material)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun delete(request: DeleteMaterialRequest, responseObserver: StreamObserver<MaterialResponse>) {
        val material = service.delete(request.id)
        val response = grpcMaterialResponse(material)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun grpcMaterialResponse(material: Material) = MaterialResponse.newBuilder()
        .setId(material.id.toString())
        .setCode(material.code)
        .setDescription(material.description)
        .setNcm(material.ncm)
        .setPlanning(material.planning)
        .setUnitPrice(material.unitPrice.toString())
        .setPricePerThousand(material.pricerPerThousand)
        .setPreShipmentLicense(material.preShipmentLicense)
        .setNcmDescription(material.ncmDescription)
        .build()
}