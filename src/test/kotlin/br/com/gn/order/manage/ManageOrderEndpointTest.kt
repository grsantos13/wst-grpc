package br.com.gn.order.manage

import br.com.gn.ManageOrderServiceGrpc
import br.com.gn.Modal
import br.com.gn.NewOrderRequest
import br.com.gn.address.Address
import br.com.gn.deliveryplace.DeliveryPlace
import br.com.gn.deliveryplace.DeliveryPlaceRepository
import br.com.gn.exporter.Exporter
import br.com.gn.exporter.ExporterRepository
import br.com.gn.exporter.Incoterm
import br.com.gn.exporter.PaymentTerms
import br.com.gn.importer.Importer
import br.com.gn.importer.ImporterRepository
import br.com.gn.material.Material
import br.com.gn.material.MaterialRepository
import br.com.gn.order.OrderRepository
import br.com.gn.order.event.EventRepository
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
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@MicronautTest(transactional = false)
internal class CreateOrderEndpointTest(
    private val grpcClient: ManageOrderServiceGrpc.ManageOrderServiceBlockingStub,
    private val exporterRepository: ExporterRepository,
    private val importerRepository: ImporterRepository,
    private val materialRepository: MaterialRepository,
    private val userRepository: UserRepository,
    private val deliveryPlaceRepository: DeliveryPlaceRepository,
    private val orderRepository: OrderRepository,
    private val eventRepository: EventRepository
) {

    private var deliveryPlace: DeliveryPlace? = null
    private var exporter: Exporter? = null
    private var importer: Importer? = null
    private var material: Material? = null
    private var user: User? = null

    @BeforeEach
    fun setup() {
        user = userRepository.save(User("email@email.com", "Teste"))
        deliveryPlace = deliveryPlaceRepository.save(DeliveryPlace("LOCAL DE ENTREGA"))
        exporter = exporterRepository.save(
            Exporter("12345678", "Test", PaymentTerms.E30, Address("Test", "test", "test", "test"), Incoterm.CIF)
        )

        importer = importerRepository.save(Importer("2422", Address("Test", "test", "test", "test")))
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
        this.material = materialRepository.save(material)
    }

    @AfterEach
    fun after() {
        eventRepository.deleteAll()
        orderRepository.deleteAll()
        exporterRepository.deleteAll()
        importerRepository.deleteAll()
        materialRepository.deleteAll()
        userRepository.deleteAll()
        deliveryPlaceRepository.deleteAll()
    }

    @Test
    fun `should save an order successfully`() {
        val request = generateNewOrderRequest()

        val response = grpcClient.create(request)
        assertNotNull(response.id)
    }

    @Test
    fun `should not save an order due to not finding the exporter`() {
        val exporterId = UUID.randomUUID().toString()
        val request = generateNewOrderRequest(exporterId = exporterId)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(request)
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Exporter not found with id $exporterId", exception.status.description)
    }

    @Test
    fun `should not save an order due to not finding the importer`() {
        val importerId = UUID.randomUUID().toString()
        val request = generateNewOrderRequest(importerId = importerId)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(request)
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Importer not found with id $importerId", exception.status.description)
    }

    @Test
    fun `should not save an order due to not finding the responsible`() {
        val responsibleId = UUID.randomUUID().toString()
        val request = generateNewOrderRequest(responsibleId = responsibleId)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(request)
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("User not found with id $responsibleId", exception.status.description)
    }

    @Test
    fun `should not save an order due to not finding the delivery place`() {
        val deliveryPlaceId = UUID.randomUUID().toString()
        val request = generateNewOrderRequest(deliveryPlaceId = deliveryPlaceId)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(request)
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Delivery place not found with id $deliveryPlaceId", exception.status.description)
    }

    @Test
    fun `should not save an order due to not finding the material`() {
        val materialId = UUID.randomUUID().toString()
        val request = generateNewOrderRequest(materialId = materialId)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(request)
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Material not found with id $materialId", exception.status.description)
    }

    @Test
    fun `should not save an order due to invalid order date`() {
        val request = generateNewOrderRequest(date = LocalDate.now().plusDays(30).toString())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(request)
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair("date", "must be a date in the past or in the present")
                )
            )
        }
    }

    @Test
    fun `should not save an order due to invalid parameters`() {
        val request = NewOrderRequest.newBuilder().build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.create(request)
        }

        val violations = violations(exception)
        violations.size
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(this), containsInAnyOrder(
                    Pair("responsibleId", "must not be blank"),
                    Pair("exporterId", "must not be blank"),
                    Pair("deliveryPlaceId", "must not be blank"),
                    Pair(
                        "importerId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair(
                        "exporterId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair("observation", "must not be blank"),
                    Pair("date", "must not be null"),
                    Pair(
                        "responsibleId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair("deadline", "must not be null"),
                    Pair("modal", "must not be null"),
                    Pair("importerId", "must not be blank"),
                    Pair("origin", "must not be blank"),
                    Pair("destination", "must not be blank"),
                    Pair("items", "size must be between 1 and 2147483647"),
                    Pair("necessity", "must not be null"),
                    Pair(
                        "deliveryPlaceId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                )
            )
        }
    }

    private fun generateNewOrderRequest(
        exporterId: String? = null,
        importerId: String? = null,
        responsibleId: String? = null,
        deliveryPlaceId: String? = null,
        materialId: String? = null,
        date: String? = null
    ) =
        NewOrderRequest.newBuilder()
            .setOrigin("EUA")
            .setDestination("Brazil")
            .setExporterId(exporterId ?: exporter!!.id.toString())
            .addItems(
                0, NewOrderRequest.Item.newBuilder()
                    .setMaterialId(materialId ?: material!!.id.toString())
                    .setQuantity("1000")
                    .build()
            )
            .setNumber("4200212121")
            .setImporterId(importerId ?: importer!!.id.toString())
            .setDate(date ?: LocalDate.now().toString())
            .setResponsibleId(responsibleId ?: user!!.id.toString())
            .setModal(Modal.SEA)
            .setNecessity(LocalDate.now().toString())
            .setDeadline(LocalDate.now().toString())
            .setObservation("Test")
            .setDeliveryPlaceId(deliveryPlaceId ?: deliveryPlace!!.id.toString())
            .build()
}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
        ManageOrderServiceGrpc.newBlockingStub(channel)
}