package br.com.gn.importer

import br.com.gn.Address
import br.com.gn.DeleteImporterRequest
import br.com.gn.ImporterServiceGrpc
import br.com.gn.NewImporterRequest
import br.com.gn.ReadImporterRequest
import br.com.gn.UpdateImporterRequest
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
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import br.com.gn.address.Address as ImporterAddress

@MicronautTest(transactional = false)
internal class ImporterEndpointTest(
    private val grpcClient: ImporterServiceGrpc.ImporterServiceBlockingStub,
    private val repository: ImporterRepository
) {

    @AfterEach
    fun after() {
        repository.deleteAll()
    }

    @Test
    fun `should create an importer successfully`() {

        val response = grpcClient.create(
            NewImporterRequest.newBuilder()
                .setAddress(
                    Address.newBuilder()
                        .setZipCode("123456789")
                        .setStreet("Avenida Invernada")
                        .setCountry("Brazil")
                        .setCity("Valinhos")
                        .build()
                )
                .setPlant("2422")
                .build()
        )

        assertNotNull(response.id)
    }

    @Test
    fun `should not create an importer due to existing code`() {

        repository.save(createImporter())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewImporterRequest.newBuilder()
                    .setAddress(
                        Address.newBuilder()
                            .setZipCode("123456789")
                            .setStreet("Avenida Invernada")
                            .setCountry("Brazil")
                            .setCity("Valinhos")
                            .build()
                    )
                    .setPlant("2422")
                    .build()
            )
        }

        assertEquals(Status.ALREADY_EXISTS.code, exception.status.code)
        assertEquals("Importer already exists with plant 2422", exception.status.description)
    }

    @Test
    fun `should not create an importer due to invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(
                NewImporterRequest.newBuilder()
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair("plant", "must not be blank"),
                    Pair("street", "must not be blank"),
                    Pair("city", "must not be blank"),
                    Pair("country", "must not be blank"),
                    Pair("zipCode", "must not be blank")
                )
            )
        }
    }

    @Test
    fun `should read by plant`() {
        repository.save(createImporter())
        repository.save(createImporter(plant = "2423"))

        val response = grpcClient.read(ReadImporterRequest.newBuilder().setPlant("2423").build())

        assertEquals(1, response.importersList.size)
    }

    @Test
    fun `should read all`() {
        repository.save(createImporter())
        repository.save(createImporter("2423"))

        val response = grpcClient.read(ReadImporterRequest.newBuilder().build())

        assertEquals(2, response.importersList.size)
    }

    @Test
    fun `should update a importer successfully`() {
        val importer = repository.save(createImporter())

        val response = grpcClient.update(
            UpdateImporterRequest.newBuilder()
                .setAddress(
                    Address.newBuilder()
                        .setZipCode("123456789")
                        .setStreet("Avenida Alterada")
                        .setCountry("Brazil")
                        .setCity("Valinhos")
                        .build()
                )
                .setId(importer.id.toString())
                .build()
        )

        assertEquals("Avenida Alterada", response.address.street)
    }

    @Test
    fun `should not update a importer due to not finding by id`() {

        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateImporterRequest.newBuilder()
                    .setAddress(
                        Address.newBuilder()
                            .setZipCode("123456789")
                            .setStreet("Avenida Invernada")
                            .setCountry("Brazil")
                            .setCity("Valinhos")
                            .build()
                    ).setId(randomId)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Importer not found with id $randomId", exception.status.description)
    }

    @Test
    fun `should not update an importer due to invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateImporterRequest.newBuilder()
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair("street", "must not be blank"),
                    Pair("city", "must not be blank"),
                    Pair("country", "must not be blank"),
                    Pair("zipCode", "must not be blank"),
                    Pair("id", "must not be blank"),
                    Pair(
                        "id",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    )
                )
            )
        }
    }

    @Test
    fun `should delete a importer successfully`() {
        val importer = repository.save(createImporter())

        assertDoesNotThrow {
            grpcClient.delete(
                DeleteImporterRequest.newBuilder()
                    .setId(importer.id.toString())
                    .build()
            )
        }
    }

    @Test
    fun `should not delete for not finding by id`() {
        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeleteImporterRequest.newBuilder()
                    .setId(randomId)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Importer not found with id $randomId", exception.status.description)
    }

    @Test
    fun `should not delete an importer due to invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeleteImporterRequest.newBuilder()
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair("id", "must not be blank"),
                    Pair(
                        "id",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    )
                )
            )
        }
    }

    private fun createImporter(plant: String? = null) =
        Importer(
            plant = plant ?: "2422",
            address = ImporterAddress("Test", "test", "test", "test")
        )

}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
        ImporterServiceGrpc.newBlockingStub(channel)
}