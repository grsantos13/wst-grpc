package br.com.gn.order.manage

import br.com.gn.*
import br.com.gn.address.Address
import br.com.gn.deliveryplace.DeliveryPlace
import br.com.gn.deliveryplace.DeliveryPlaceRepository
import br.com.gn.exporter.Currency
import br.com.gn.exporter.Exporter
import br.com.gn.exporter.ExporterRepository
import br.com.gn.exporter.Incoterm
import br.com.gn.exporter.PaymentTerms
import br.com.gn.importer.Importer
import br.com.gn.importer.ImporterRepository
import br.com.gn.material.Material
import br.com.gn.material.MaterialRepository
import br.com.gn.order.Item
import br.com.gn.order.Order
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
import br.com.gn.order.Modal as OrderModal

@MicronautTest(transactional = false)
internal class ManageOrderEndpointTest(
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
            Exporter(
                code = "12345678",
                name = "Test",
                paymentTerms = PaymentTerms.E30,
                address = Address("Test", "test", "test", "test"),
                incoterm = Incoterm.CIF,
                currency = Currency.EUR,
                availabilityLT = 30,
                departureLT = 6,
                arrivalLT = 20,
                totalLT = 80
            )
        )

        importer = importerRepository.save(Importer(
            plant = "2422",
            fiscalName = "COMPANY LLC",
            fiscalNumber = "27679970000111",
            address = Address("Test", "test", "test", "test")
        ))
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

    @Test
    fun `should update an order successfully`() {
        val createdOrder = orderRepository.save(generateOrder())
        val newResponsible = userRepository.save(User("teste@teste.com", "Teste Update"))
        val newDeliveryPlace = deliveryPlaceRepository.save(DeliveryPlace("Delivery Place 2"))
        val response = grpcClient.update(
            UpdateOrderRequest.newBuilder()
                .setDeadline("2021-07-18")
                .setDeliveryPlaceId(newDeliveryPlace.id.toString())
                .setId(createdOrder.id.toString())
                .setModal(Modal.ROAD)
                .setNecessity("2021-07-01")
                .setResponsibleId(newResponsible.id.toString())
                .build()
        )

        assertEquals("2021-07-18", response.deadline)
        assertEquals(newDeliveryPlace.name, response.deliveryPlace)
        assertEquals(Modal.ROAD, response.modal)
        assertEquals("2021-07-01", response.necessity)
        assertEquals(newResponsible.name, response.responsible.name)
    }

    @Test
    fun `should not update an order for not finding the delivery place`() {
        val createdOrder = orderRepository.save(generateOrder())
        val newResponsible = userRepository.save(User("teste@teste.com", "Teste Update"))
        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateOrderRequest.newBuilder()
                    .setDeadline(LocalDate.now().plusDays(1).toString())
                    .setDeliveryPlaceId(randomId)
                    .setId(createdOrder.id.toString())
                    .setModal(Modal.ROAD)
                    .setNecessity(LocalDate.now().plusMonths(3).toString())
                    .setResponsibleId(newResponsible.id.toString())
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Delivery place not found with id $randomId", exception.status.description)

    }

    @Test
    fun `should not update an order for not finding the order`() {
        val newResponsible = userRepository.save(User("teste@teste.com", "Teste Update"))
        val newDeliveryPlace = deliveryPlaceRepository.save(DeliveryPlace("Delivery Place 2"))
        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateOrderRequest.newBuilder()
                    .setDeadline(LocalDate.now().plusDays(1).toString())
                    .setDeliveryPlaceId(newDeliveryPlace.id.toString())
                    .setId(randomId)
                    .setModal(Modal.ROAD)
                    .setNecessity(LocalDate.now().plusMonths(3).toString())
                    .setResponsibleId(newResponsible.id.toString())
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Order not found with id $randomId", exception.status.description)

    }

    @Test
    fun `should not update an order for not finding the responsible`() {
        val createdOrder = orderRepository.save(generateOrder())
        val newDeliveryPlace = deliveryPlaceRepository.save(DeliveryPlace("Delivery Place 2"))
        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateOrderRequest.newBuilder()
                    .setDeadline(LocalDate.now().plusDays(1).toString())
                    .setDeliveryPlaceId(newDeliveryPlace.id.toString())
                    .setId(createdOrder.id.toString())
                    .setModal(Modal.ROAD)
                    .setNecessity(LocalDate.now().plusMonths(3).toString())
                    .setResponsibleId(randomId)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("User not found with id $randomId", exception.status.description)

    }

    @Test
    fun `should not update an order due to invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.update(
                UpdateOrderRequest.newBuilder()
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Arguments validation error", status.description)
            assertThat(
                violations(exception), containsInAnyOrder(
                    Pair(
                        "deliveryPlaceId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair(
                        "id",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair("necessity", "must not be blank"),
                    Pair("responsibleId", "must not be blank"),
                    Pair("modal", "must not be null"),
                    Pair("id", "must not be blank"),
                    Pair(
                        "responsibleId",
                        "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\""
                    ),
                    Pair("deadline", "must not be blank"),
                )
            )
        }
    }

    @Test
    fun `should update an observation successfully`() {
        val order = orderRepository.save(generateOrder())
        val obs = "Updating observation"
        val response = grpcClient.updateObs(
            UpdateObsOrderRequest.newBuilder()
                .setId(order.id.toString())
                .setObservation(obs)
                .build()
        )

        assertEquals(obs, response.observation)
    }

    @Test
    fun `should not update an observation for not finding the order`() {
        val obs = "Updating observation"
        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.updateObs(
                UpdateObsOrderRequest.newBuilder()
                    .setId(randomId)
                    .setObservation(obs)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Order not found with id $randomId", exception.status.description)
    }

    @Test
    fun `should not update an observation for invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.updateObs(
                UpdateObsOrderRequest.newBuilder()
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
                    Pair("id", "must not be blank"),
                    Pair("observation", "must not be blank"),
                )
            )
        }

    }

    @Test
    fun `should delete an order successfully`() {
        val order = orderRepository.save(generateOrder())
        grpcClient.delete(
            DeleteOrderRequest.newBuilder()
                .setId(order.id.toString())
                .build()
        )
    }

    @Test
    fun `should not delete an order for not finding the order`() {
        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeleteOrderRequest.newBuilder()
                    .setId(randomId)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Order not found with id $randomId", exception.status.description)
    }

    @Test
    fun `should update the reference successfully`() {
        val order = orderRepository.save(generateOrder())
        val reference = "AB-0123-45"
        val response = grpcClient.updateRef(
            UpdateRefOrderRequest.newBuilder()
                .setId(order.id.toString())
                .setReference(reference)
                .build()
        )

        assertEquals(reference, response.brokerReference)
    }

    @Test
    fun `should not update the reference for not finding the order`() {
        val obs = "AB-0123-45"
        val randomId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.updateRef(
                UpdateRefOrderRequest.newBuilder()
                    .setId(randomId)
                    .setReference(obs)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals("Order not found with id $randomId", exception.status.description)
    }

    @Test
    fun `should not update the reference for invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.updateRef(
                UpdateRefOrderRequest.newBuilder()
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
                    Pair("id", "must not be blank"),
                    Pair("reference", "must not be blank"),
                )
            )
        }

    }

    @Test
    fun `should not update the reference because it already exists`() {
        orderRepository.save(generateOrder())
            .apply {
                updateReference("AB-0123-45")
                orderRepository.update(this)
            }
        val order = orderRepository.save(generateOrder("4201010102"))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.updateRef(
                UpdateRefOrderRequest.newBuilder()
                    .setId(order.id.toString())
                    .setReference("AB-0123-45")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Reference AB-0123-45 already exists", status.description)
        }

    }

    @Test
    fun `should not delete an order for invalid parameters`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeleteOrderRequest.newBuilder()
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
                    Pair("id", "must not be blank"),
                )
            )
        }

    }

    private fun generateOrder(number: String? = null) = Order(
        origin = "USA",
        destination = "Brazil",
        exporter = exporter!!,
        number = number ?: "4201010101",
        importer = importer!!,
        date = LocalDate.now(),
        responsible = user!!,
        modal = OrderModal.SEA,
        necessity = LocalDate.now(),
        deadline = LocalDate.now()
    ).apply {
        includeItems(listOf(Item(BigDecimal.TEN, material!!, this)))
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
            .setRoute("MAR_USA_EXP_IMP")
            .setDeliveryPlaceId(deliveryPlaceId ?: deliveryPlace!!.id.toString())
            .build()
}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
        ManageOrderServiceGrpc.newBlockingStub(channel)
}