package br.com.gn.operation

import br.com.gn.DeleteOperationRequest
import br.com.gn.NewOperationRequest
import br.com.gn.OperationServiceGrpc
import br.com.gn.OperationType
import br.com.gn.ReadOperationRequest
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
import br.com.gn.operation.OperationType as Type

@MicronautTest(transactional = false)
internal class OperationEndpointTest(
    private val grpcClient: OperationServiceGrpc.OperationServiceBlockingStub,
    private val repository: OperationRepository
) {

    @AfterEach
    fun after() = repository.deleteAll()

    @Test
    fun `should create a operation successfully`() {
        val response = grpcClient.create(
            NewOperationRequest.newBuilder()
                .setCountry("Brazil")
                .setType(OperationType.IMPORT)
                .build()
        )

        assertNotNull(response.id)
    }

    @Test
    fun `should return an error for not informing country or type`() {
        val exceptionMissingCountry = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewOperationRequest.newBuilder()
                    .setType(OperationType.IMPORT)
                    .build()
            )
        }

        val exceptionMissingType = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewOperationRequest.newBuilder()
                    .setCountry("Brazil")
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exceptionMissingCountry.status.code)
        assertEquals("Country must not be blank", exceptionMissingCountry.status.description)
        assertEquals(Status.INVALID_ARGUMENT.code, exceptionMissingType.status.code)
        assertEquals("Type must not be blank", exceptionMissingType.status.description)

    }

    @Test
    fun `should return an error for existing operation with same country and type`() {
        repository.save(Operation("Brazil", Type.IMPORT))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewOperationRequest.newBuilder()
                    .setCountry("Brazil")
                    .setType(OperationType.IMPORT)
                    .build()
            )
        }

        assertEquals(Status.ALREADY_EXISTS.code, exception.status.code)
        assertEquals("Operation with country Brazil and type IMPORT already exists", exception.status.description)
    }

    @Test
    fun `should return all created operations`() {
        repository.save(Operation("Brazil", Type.IMPORT))
        repository.save(Operation("Chile", Type.EXPORT))

        val response = grpcClient.read(ReadOperationRequest.newBuilder().build())

        assertEquals(2, response.operationsList.size)
    }

    @Test
    fun `should return an empty list when no operation is found`() {
        val response = grpcClient.read(ReadOperationRequest.newBuilder().build())

        assertEquals(0, response.operationsList.size)
    }

    @Test
    fun `should delete an operation successfully`() {
        val operation = Operation("Brazil", Type.IMPORT)
        repository.save(operation)
        grpcClient.delete(DeleteOperationRequest.newBuilder().setId(operation.id.toString()).build())
    }

    @Test
    fun `should not delete an operation for not finding by id`() {
        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeleteOperationRequest.newBuilder().setId(randomId).build())
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Operation not found with id $randomId", exception.status.description)
    }

    @Test
    fun `should not delete an operation for not informing the id`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeleteOperationRequest.newBuilder().build())
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
        assertEquals("Id must not be blank", exception.status.description)
    }
}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) managedChannel: ManagedChannel) =
        OperationServiceGrpc.newBlockingStub(managedChannel)
}