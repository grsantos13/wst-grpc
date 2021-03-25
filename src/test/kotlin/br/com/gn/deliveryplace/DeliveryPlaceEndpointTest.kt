package br.com.gn.deliveryplace

import br.com.gn.DeleteDeliveryPlaceRequest
import br.com.gn.DeliveryPlaceServiceGrpc
import br.com.gn.NewDeliveryPlaceRequest
import br.com.gn.ReadDeliveryPlaceRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class DeliveryPlaceEndpointTest(
    private val client: DeliveryPlaceServiceGrpc.DeliveryPlaceServiceBlockingStub,
    private val repository: DeliveryPlaceRepository
) {

    @AfterEach
    fun after() {
        repository.deleteAll()
    }

    @Test
    fun `should create a delivery place successfully`() {
        val response = client.create(
            NewDeliveryPlaceRequest.newBuilder()
                .setName("LOCAL DE ENTREGA")
                .build()
        )

        assertEquals("LOCAL DE ENTREGA", response.name)
        assertNotNull(response.id)
    }

    @Test
    fun `should not create a delivery place due to existing name`() {
        repository.save(DeliveryPlace("LOCAL DE ENTREGA"))

        val exception = assertThrows<StatusRuntimeException> {
            client.create(
                NewDeliveryPlaceRequest.newBuilder()
                    .setName("LOCAL DE ENTREGA")
                    .build()
            )
        }

        assertEquals(Status.ALREADY_EXISTS.code, exception.status.code)
        assertEquals("Delivery place already exists with name LOCAL DE ENTREGA", exception.status.description)
    }

    @Test
    fun `should not create a delivery place due to invalid argument`() {
        val exception = assertThrows<StatusRuntimeException> {
            client.create(
                NewDeliveryPlaceRequest.newBuilder()
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
        assertEquals("Name must be informed", exception.status.description)
    }


    @Test
    fun `should return all the delivery places when a delivery place is created`() {
        repository.save(DeliveryPlace("LOCAL DE ENTREGA0"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA1"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA2"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA3"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA4"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA5"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA6"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA7"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA8"))
        repository.save(DeliveryPlace("LOCAL DE ENTREGA9"))

        val response = client.read(ReadDeliveryPlaceRequest.newBuilder().build())
        assertEquals(10, response.placesList.size)
    }


    @Test
    fun `should return an empty list when no delivery place is created`() {
        val response = client.read(ReadDeliveryPlaceRequest.newBuilder().build())
        assertEquals(0, response.placesList.size)
    }

    @Test
    fun `should delete a delivery place successfully`() {
        val createdDeliveryPlace = repository.save(DeliveryPlace("LOCAL DE ENTREGA"))

        client.delete(
            DeleteDeliveryPlaceRequest.newBuilder()
                .setId(createdDeliveryPlace.id.toString())
                .build()
        )
    }

    @Test
    fun `should not delete a delivery place for not finding it`() {
        val invalidId = UUID.randomUUID().toString()

        val exception = assertThrows<StatusRuntimeException> {
            client.delete(
                DeleteDeliveryPlaceRequest.newBuilder()
                    .setId(invalidId)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Delivery place not found with id $invalidId", exception.status.description)
    }
}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) managedChannel: ManagedChannel) =
        DeliveryPlaceServiceGrpc.newBlockingStub(managedChannel)
}