package br.com.gn.user

import br.com.gn.DeleteUserRequest
import br.com.gn.NewUserRequest
import br.com.gn.ReadUserRequest
import br.com.gn.UpdateUserRequest
import br.com.gn.UserServiceGrpc
import com.google.protobuf.Any
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class UserEndpointTest(
    val client: UserServiceGrpc.UserServiceBlockingStub,
    val repository: UserRepository
) {

    @AfterEach
    fun after() {
        repository.deleteAll()
    }

    @Test
    fun `should create a user successfully`() {
        val response = client.create(
            NewUserRequest.newBuilder()
                .setEmail("email@email.com")
                .setName("Email")
                .build()
        )

        assertEquals("email@email.com", response.email)
        assertEquals("Email", response.name)
    }

    @Test
    fun `should return a validation error`() {
        val exception = assertThrows<StatusRuntimeException> {
            client.create(
                NewUserRequest.newBuilder()
                    .setEmail("")
                    .setName("")
                    .build()
            )
        }

        val anyDetails: Any? = StatusProto.fromThrowable(exception)
            ?.detailsList?.get(0)

        val badRequest = anyDetails!!.unpack(BadRequest::class.java)
        assertEquals(3, badRequest.fieldViolationsList.size)
        assertTrue(
            badRequest.fieldViolationsList.contains(
                generateFieldViolation("email", "must be a well-formed email address")
            )
        )
        assertTrue(badRequest.fieldViolationsList.contains(generateFieldViolation("email", "must not be blank")))
        assertTrue(badRequest.fieldViolationsList.contains(generateFieldViolation("name", "must not be blank")))
        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
        assertEquals("Arguments validation error", exception.status.description)
    }


    @Test
    fun `should filter by name`() {
        val createdUser = createUser()
        repository.save(createdUser)

        val response = client.read(
            ReadUserRequest.newBuilder()
                .setName("Email")
                .build()
        )

        assertEquals(1, response.usersList.size)

    }

    @Test
    fun `should filter by all`() {
        val createdUser = createUser()
        repository.save(createdUser)

        val response = client.read(
            ReadUserRequest.newBuilder()
                .build()
        )

        assertEquals(1, response.usersList.size)

    }

    @Test
    fun `should filter by email`() {
        val createdUser = createUser()
        repository.save(createdUser)

        val response = client.read(
            ReadUserRequest.newBuilder()
                .setEmail("email@email.com")
                .build()
        )

        assertEquals(1, response.usersList.size)

    }

    @Test
    fun `should return error due to invalid field`() {
        val createdUser = createUser()
        repository.save(createdUser)

        val exceptionEmail = assertThrows<StatusRuntimeException> {
            client.read(
                ReadUserRequest.newBuilder()
                    .setEmail("")
                    .build()
            )
        }
        val exceptionName = assertThrows<StatusRuntimeException> {
            client.read(
                ReadUserRequest.newBuilder()
                    .setName("")
                    .build()
            )
        }



        assertEquals(Status.INVALID_ARGUMENT.code, exceptionEmail.status.code)
        assertEquals("Email must be informed for a filter", exceptionEmail.status.description)
        assertEquals(Status.INVALID_ARGUMENT.code, exceptionName.status.code)
        assertEquals("Name must be informed for a filter", exceptionName.status.description)

    }

    @Test
    fun `should update a user successfully`() {
        val createdUser = createUser()
        repository.save(createdUser)

        val update = client.update(
            UpdateUserRequest.newBuilder()
                .setEmail("emailatualizado@email.com")
                .setId(createdUser.id.toString())
                .build()
        )

        assertEquals("emailatualizado@email.com", update.email)
    }

    @Test
    fun `should not update a user when the email already exists`() {
        val createdUser = createUser()
        repository.save(createdUser)

        val exception = assertThrows<StatusRuntimeException> {
            client.update(
                UpdateUserRequest.newBuilder()
                    .setEmail("email@email.com")
                    .setId(createdUser.id.toString())
                    .build()
            )
        }

        assertEquals(Status.ALREADY_EXISTS.code, exception.status.code)
        assertEquals("User already exists with email email@email.com", exception.status.description)
    }

    @Test
    fun `should not update a user when the user is not found`() {
        val createdUser = createUser()
        repository.save(createdUser)

        val idNotFound = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            client.update(
                UpdateUserRequest.newBuilder()
                    .setEmail("emailatualizado@email.com")
                    .setId(idNotFound)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("User not found with id $idNotFound", exception.status.description)
    }

    @Test
    fun `should not update due to invalid parameters`() {
        val createdUser = createUser()
        repository.save(createdUser)

        val exception = assertThrows<StatusRuntimeException> {
            client.update(
                UpdateUserRequest.newBuilder()
                    .setEmail("")
                    .setId("")
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
        assertEquals("Arguments validation error", exception.status.description)
    }

    @Test
    fun `should delete a user successfully`() {
        val createdUser = createUser()
        repository.save(createdUser)

        client.delete(
            DeleteUserRequest.newBuilder()
                .setId(createdUser.id.toString())
                .build()
        )

    }

    @Test
    fun `should not delete a user when the user is not found`() {
        val idNotFound = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            client.delete(
                DeleteUserRequest.newBuilder()
                    .setId(idNotFound)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("User not found with id $idNotFound", exception.status.description)
    }

    private fun createUser() = User("email@email.com", "Email")


    private fun generateFieldViolation(field: String, description: String) = BadRequest.FieldViolation.newBuilder()
        .setField(field)
        .setDescription(description)
        .build()
}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
        UserServiceGrpc.newBlockingStub(channel)
}