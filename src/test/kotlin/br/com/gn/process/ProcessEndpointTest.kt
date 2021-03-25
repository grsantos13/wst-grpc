package br.com.gn.process

import br.com.gn.DeleteProcessRequest
import br.com.gn.NewProcessRequest
import br.com.gn.ProcessServiceGrpc
import br.com.gn.ReadProcessRequest
import br.com.gn.UpdateProcessRequest
import br.com.gn.operation.Operation
import br.com.gn.operation.OperationRepository
import br.com.gn.operation.OperationType
import br.com.gn.user.User
import br.com.gn.user.UserRepository
import br.com.gn.util.StatusRuntimeExceptionUtils.Companion.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ProcessEndpointTest(
    private val repository: ProcessRepository,
    private val userRepository: UserRepository,
    private val operationRepository: OperationRepository,
    private val grpcClient: ProcessServiceGrpc.ProcessServiceBlockingStub
) {

    private var user: User? = null
    private var operation: Operation? = null

    @BeforeEach
    fun setup() {
        user = userRepository.save(User("email@email.com", "Teste"))
        operation = operationRepository.save(Operation("Brazil", OperationType.IMPORT))
    }

    @AfterEach
    fun after() {
        repository.deleteAll()
        operationRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should create a process successfully`() {
        val request = NewProcessRequest.newBuilder()
            .setName("AMOSTRA")
            .setOperationId(operation!!.id.toString())
            .setResponsibleId(user!!.id.toString())
            .build()

        val response = grpcClient.create(request)
        assertNotNull(response.id)
    }

    @Test
    fun `should not create a due to not finding the operation`() {
        val operationId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewProcessRequest.newBuilder()
                    .setName("AMOSTRA")
                    .setOperationId(operationId)
                    .setResponsibleId(user!!.id.toString())
                    .build()
            )

        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Operation not found with id $operationId", exception.status.description)
    }

    @Test
    fun `should not create a due to not finding the responsible`() {
        val responsibleId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewProcessRequest.newBuilder()
                    .setName("AMOSTRA")
                    .setOperationId(operation!!.id.toString())
                    .setResponsibleId(responsibleId)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Responsible not found with id $responsibleId", exception.status.description)
    }

    @Test
    fun `should not create a for existing a process with same name`() {
        repository.save(Process(user!!, "AMOSTRA", operation!!))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewProcessRequest.newBuilder()
                    .setName("AMOSTRA")
                    .setOperationId(operation!!.id.toString())
                    .setResponsibleId(user!!.id.toString())
                    .build()
            )
        }

        assertEquals(Status.ALREADY_EXISTS.code, exception.status.code)
        assertEquals("Process already exists with name AMOSTRA", exception.status.description)
    }

    @Test
    fun `should not create a due to invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewProcessRequest.newBuilder()
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair(
                        "responsibleId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair("responsibleId", "must not be blank"),
                    Pair("operationId", "must not be blank"),
                    Pair(
                        "operationId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair("name", "must not be blank")
                )
            )
        }
    }

    @Test
    fun `should read all`() {
        repository.save(Process(user!!, "AMOSTRA", operation!!))
        repository.save(Process(user!!, "NORMAL", operation!!))
        repository.save(Process(user!!, "PROJETO", operation!!))

        val response = grpcClient.read(ReadProcessRequest.newBuilder().build())
        assertEquals(3, response.processesList.size)
    }

    @Test
    fun `should read by name`() {
        repository.save(Process(user!!, "AMOSTRA", operation!!))
        repository.save(Process(user!!, "NORMAL", operation!!))
        repository.save(Process(user!!, "PROJETO", operation!!))

        val response = grpcClient.read(ReadProcessRequest.newBuilder().setName("AMOSTRA").build())
        assertEquals(1, response.processesList.size)
        assertEquals("AMOSTRA", response.processesList[0].name)
    }

    @Test
    fun `should update a process successfully`() {
        val createdProcess = repository.save(Process(user!!, "AMOSTRA", operation!!))
        val newResponsible = userRepository.save(User("test@test.com", "Update test"))
        val response = grpcClient.update(
            UpdateProcessRequest.newBuilder().setResponsibleId(newResponsible.id.toString())
                .setId(createdProcess.id.toString())
                .build()
        )

        assertEquals("Update test", response.responsible)
    }

    @Test
    fun `should not update a process due to not finding the process`() {
        val processId = UUID.randomUUID().toString()
        val newResponsible = userRepository.save(User("test@test.com", "Update test"))
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateProcessRequest.newBuilder().setResponsibleId(newResponsible.id.toString())
                    .setId(processId)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Process not found with id $processId", exception.status.description)
    }

    @Test
    fun `should not update a process due to not finding the responsible`() {
        val responsibleId = UUID.randomUUID().toString()
        val createdProcess = repository.save(Process(user!!, "AMOSTRA", operation!!))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateProcessRequest.newBuilder()
                    .setResponsibleId(responsibleId)
                    .setId(createdProcess.id.toString())
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Responsible not found with id $responsibleId", exception.status.description)
    }

    @Test
    fun `should not update a process due to invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateProcessRequest.newBuilder()
                    .build()
            )
        }

        with(exception) {

            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair(
                        "responsibleId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair(
                        "id",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair("responsibleId", "must not be blank"),
                    Pair("id", "must not be blank")
                )
            )

        }
    }

    @Test
    fun `should delete a process successfully`() {
        val createdProcess = repository.save(Process(user!!, "AMOSTRA", operation!!))
        assertDoesNotThrow {
            grpcClient.delete(
                DeleteProcessRequest.newBuilder()
                    .setId(createdProcess.id.toString())
                    .build()
            )
        }
    }

    @Test
    fun `should not delete a process due to not finding the process`() {
        val processId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeleteProcessRequest.newBuilder()
                    .setId(processId)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Process not found with id $processId", exception.status.description)
    }

    @Test
    fun `should not delete a process due to invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeleteProcessRequest.newBuilder()
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair(
                        "id",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair("id", "must not be blank")
                )
            )
        }
    }
}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
        ProcessServiceGrpc.newBlockingStub(channel)
}