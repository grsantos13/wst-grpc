package br.com.gn.route.point

import br.com.gn.*
import br.com.gn.shared.exception.ErrorHandler
import br.com.gn.shared.exception.ObjectAlreadyExistsException
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional

@ErrorHandler
@Singleton
class PointEndpoint(
    private val manager: EntityManager
) : PointServiceGrpc.PointServiceImplBase() {

    @Transactional
    override fun create(request: NewPointRequest, responseObserver: StreamObserver<PointResponse>) {
        if (request.name.isNullOrBlank())
            throw IllegalArgumentException("Name must not be blank")

        if (manager.createQuery(" select 1 from Point where name = :name ")
                .setParameter("name", request.name)
                .resultList.isNotEmpty()
        )
            throw ObjectAlreadyExistsException("Point already exists with name ${request.name}")

        val point = Point(request.name)
        manager.persist(point)
        responseObserver.onNext(
            PointResponse.newBuilder()
                .setId(point.id.toString())
                .setName(point.name)
                .build()
        )
        responseObserver.onCompleted()
    }

    @Transactional
    override fun read(request: ReadPointRequest, responseObserver: StreamObserver<PointsResponse>) {
        val list = manager.createQuery(" select p from Point p ", Point::class.java)
            .resultList
            .map {
                PointResponse.newBuilder()
                    .setId(it.id.toString())
                    .setName(it.name)
                    .build()
            }

        responseObserver.onNext(
            PointsResponse.newBuilder()
                .addAllPoint(list)
                .build()
        )
        responseObserver.onCompleted()
    }
}