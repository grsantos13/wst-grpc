package br.com.gn.material

import br.com.gn.MaterialServiceGrpc
import br.com.gn.NewMaterialRequest.newBuilder
import br.com.gn.OrderDirection
import br.com.gn.Pageable
import br.com.gn.ReadMaterialRequest
import br.com.gn.UpdateMaterialRequest
import br.com.gn.client.NcmSearchRequest
import br.com.gn.client.NcmSearchResponse
import br.com.gn.client.NcmSiscomexClient
import br.com.gn.util.StatusRuntimeExceptionUtils.Companion.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.math.BigDecimal

@MicronautTest(transactional = false)
internal class MaterialEndpointTest(
    private val grpcClient: MaterialServiceGrpc.MaterialServiceBlockingStub,
    private val ncmSiscomexClient: NcmSiscomexClient,
    private val repository: MaterialRepository
) {

    @AfterEach
    fun after() {
        repository.deleteAll()
    }

    @Test
    fun `should create a material successfully`() {
        val request = newBuilder()
            .setCode("12345678")
            .setDescription("Material teste")
            .setNcm("88026000")
            .setPlanning("Gustavo")
            .setPreShipmentLicense(false)
            .setPricePerThousand(true)
            .setUnitPrice("1290")
            .build()

        Mockito.`when`(ncmSiscomexClient.search(NcmSearchRequest(request.ncm)))
            .thenReturn(
                listOf(
                    NcmSearchResponse(
                        codigo = request.ncm,
                        fimVigencia = null,
                        fragmentosEncontrados = listOf("- Veículos espaciais (incluindo os satélites) e seus veículos de lançamento, e veículos suborbitais"),
                        inicioVigencia = null,
                        nivelHierarquico = "Sub-Item",
                        nomeExtenso = "- Veículos espaciais (incluindo os satélites) e seus veículos de lançamento, e veículos suborbitais",
                        possuiFilhos = true
                    )
                )
            )

        val response = grpcClient.create(request)

        assertNotNull(response.id)
        assertEquals(
            "- Veículos espaciais (incluindo os satélites) e seus veículos de lançamento, e veículos suborbitais",
            response.ncmDescription
        )
    }

    @Test
    fun `should not create a material due to not finding ncm on Siscomex`() {
        val request = newBuilder()
            .setCode("12345678")
            .setDescription("Material teste")
            .setNcm("88026000")
            .setPlanning("Gustavo")
            .setPreShipmentLicense(false)
            .setPricePerThousand(true)
            .setUnitPrice("1290")
            .build()

        Mockito.`when`(ncmSiscomexClient.search(NcmSearchRequest(request.ncm)))
            .thenReturn(listOf())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(request)
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
        assertEquals("Ncm ${request.ncm} not found in Siscomex", exception.status.description)

    }

    @Test
    fun `should not create a material due to invalid arguments`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(newBuilder().build())
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair("unitPrice", "must be greater than 0"),
                    Pair("ncm", "must match \"[0-9]{8}\""),
                    Pair("planning", "must not be blank"),
                    Pair("code", "must not be blank"),
                    Pair("ncm", "must not be blank"),
                    Pair("description", "must not be blank")
                )
            )

        }
    }

    @Test
    fun `should return materials by ncm paged`() {
        val material = createMaterial()
        repository.save(material)

        val request = ReadMaterialRequest.newBuilder()
            .setNcm(material.ncm)
            .setPageable(generatePageable())
            .build()

        val response = grpcClient.read(request)
        assertEquals(1, response.materialsList.size)
    }

    @Test
    fun `should return materials by code paged`() {
        val material = createMaterial()
        repository.save(material)

        val request = ReadMaterialRequest.newBuilder()
            .setCode(material.code)
            .setPageable(generatePageable())
            .build()

        val response = grpcClient.read(request)
        assertEquals(1, response.materialsList.size)
    }

    @Test
    fun `should return materials by description paged`() {
        val material = createMaterial()
        repository.save(material)

        val request = ReadMaterialRequest.newBuilder()
            .setDescription(material.description.substring(0, 7))
            .setPageable(generatePageable())
            .build()

        val response = grpcClient.read(request)
        assertEquals(1, response.materialsList.size)
    }

    @Test
    fun `should return all the materials paged`() {
        val material = createMaterial()
        repository.save(material)

        val request = ReadMaterialRequest.newBuilder()
            .setPageable(generatePageable())
            .build()

        val response = grpcClient.read(request)
        assertEquals(1, response.materialsList.size)
    }

    @Test
    fun `should return an error due to invalid filter`() {
        val material = createMaterial()
        repository.save(material)

        val exceptionByDescription = assertThrows<StatusRuntimeException> {
            grpcClient.read(
                ReadMaterialRequest.newBuilder()
                    .setDescription("")
                    .setPageable(generatePageable())
                    .build()
            )
        }

        val exceptionByNcm = assertThrows<StatusRuntimeException> {
            grpcClient.read(
                ReadMaterialRequest.newBuilder()
                    .setNcm("")
                    .setPageable(generatePageable())
                    .build()
            )
        }

        val exceptionByCode = assertThrows<StatusRuntimeException> {
            grpcClient.read(
                ReadMaterialRequest.newBuilder()
                    .setCode("")
                    .setPageable(generatePageable())
                    .build()
            )
        }

        val exceptionByPageable = assertThrows<StatusRuntimeException> {
            grpcClient.read(
                ReadMaterialRequest.newBuilder()
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exceptionByDescription.status.code)
        assertEquals(Status.INVALID_ARGUMENT.code, exceptionByNcm.status.code)
        assertEquals(Status.INVALID_ARGUMENT.code, exceptionByCode.status.code)
        assertEquals(Status.INVALID_ARGUMENT.code, exceptionByPageable.status.code)
    }

    @Test
    fun `should update a material successfully`() {
        val createMaterial = createMaterial()
        repository.save(createMaterial)

        val request = UpdateMaterialRequest.newBuilder()
            .setId(createMaterial.id.toString())
            .setDescription("Material teste Alterado")
            .setNcm("88026000")
            .setPlanning("Gustavo")
            .setPreShipmentLicense(false)
            .setPricePerThousand(true)
            .setUnitPrice("1290")
            .build()

        Mockito.`when`(ncmSiscomexClient.search(NcmSearchRequest(request.ncm)))
            .thenReturn(
                listOf(
                    NcmSearchResponse(
                        codigo = request.ncm,
                        fimVigencia = null,
                        fragmentosEncontrados = listOf("- Veículos espaciais (incluindo os satélites) e seus veículos de lançamento, e veículos suborbitais"),
                        inicioVigencia = null,
                        nivelHierarquico = "Sub-Item",
                        nomeExtenso = "- Veículos espaciais (incluindo os satélites) e seus veículos de lançamento, e veículos suborbitais",
                        possuiFilhos = true
                    )
                )
            )

        val response = grpcClient.update(request)

        assertEquals("Material teste Alterado", response.description)
    }

    @Test
    fun `should not update a material due to invalid arguments`() {
        val createMaterial = createMaterial()
        repository.save(createMaterial)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateMaterialRequest.newBuilder().setId(
                    createMaterial.id.toString()
                ).build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair("unitPrice", "must be greater than 0"),
                    Pair("ncm", "must match \"[0-9]{8}\""),
                    Pair("planning", "must not be blank"),
                    Pair("ncm", "must not be blank"),
                    Pair("description", "must not be blank")
                )
            )
        }
    }

    @Test
    fun `should not update a material due to not finding ncm on Siscomex`() {
        val createMaterial = createMaterial()
        repository.save(createMaterial)

        val request = UpdateMaterialRequest.newBuilder()
            .setId(createMaterial.id.toString())
            .setDescription("Material teste Alterado")
            .setNcm("88026000")
            .setPlanning("Gustavo")
            .setPreShipmentLicense(false)
            .setPricePerThousand(true)
            .setUnitPrice("1290")
            .build()

        Mockito.`when`(ncmSiscomexClient.search(NcmSearchRequest(request.ncm)))
            .thenReturn(listOf())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(request)
        }

        assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
        assertEquals("Ncm ${request.ncm} not found in Siscomex", exception.status.description)

    }

    private fun generatePageable() = Pageable.newBuilder()
        .setDirection(OrderDirection.DESC)
        .setPage(0)
        .setOrderBy("id")
        .setSize(10)
        .build()

    private fun createMaterial(): Material {

        val material = Material(
            code = "12345678",
            description = "Material teste",
            ncm = "88026000",
            unitPrice = BigDecimal.TEN,
            pricerPerThousand = false,
            preShipmentLicense = false,
            planning = "Gustavo"
        )

        material.updateNcmDescription("Description ncm")
        return material
    }

    @MockBean(NcmSiscomexClient::class)
    fun ncmSiscomexClient(): NcmSiscomexClient {
        return Mockito.mock(NcmSiscomexClient::class.java)
    }
}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
        MaterialServiceGrpc.newBlockingStub(channel)
}