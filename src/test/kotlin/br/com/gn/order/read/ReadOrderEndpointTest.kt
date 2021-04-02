package br.com.gn.order.read

import br.com.gn.OrderDirection
import br.com.gn.Pageable
import br.com.gn.ReadOrderRequest
import br.com.gn.ReadOrderServiceGrpc
import br.com.gn.address.Address
import br.com.gn.deliveryplace.DeliveryPlace
import br.com.gn.deliveryplace.DeliveryPlaceRepository
import br.com.gn.exporter.*
import br.com.gn.importer.Importer
import br.com.gn.importer.ImporterRepository
import br.com.gn.material.Material
import br.com.gn.material.MaterialRepository
import br.com.gn.order.Item
import br.com.gn.order.Modal
import br.com.gn.order.Order
import br.com.gn.order.OrderRepository
import br.com.gn.order.event.EventRepository
import br.com.gn.user.User
import br.com.gn.user.UserRepository
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

@MicronautTest(transactional = false)
internal class ReadOrderEndpointTest(
    private val grpcClient: ReadOrderServiceGrpc.ReadOrderServiceBlockingStub,
    private val exporterRepository: ExporterRepository,
    private val importerRepository: ImporterRepository,
    private val materialRepository: MaterialRepository,
    private val userRepository: UserRepository,
    private val deliveryPlaceRepository: DeliveryPlaceRepository,
    private val orderRepository: OrderRepository,
    private val eventRepository: EventRepository
) {

    private var deliveryPlace: DeliveryPlace? = null
    private var exporterOrderOne: Exporter? = null
    private var exporterOrderTwo: Exporter? = null
    private var importerOrderOne: Importer? = null
    private var importerOrderTwo: Importer? = null
    private var material: Material? = null
    private var user: User? = null
    private var orderOne: Order? = null
    private var orderTwo: Order? = null

    @BeforeEach
    fun setup() {
        user = userRepository.save(User("email@email.com", "Teste"))
        deliveryPlace = deliveryPlaceRepository.save(DeliveryPlace("LOCAL DE ENTREGA"))
        exporterOrderOne = exporterRepository.save(
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
        exporterOrderTwo = exporterRepository.save(
            Exporter(
                code = "12345677",
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
        importerOrderOne = importerRepository.save(Importer(
            plant = "2422",
            fiscalName = "COMPANY LLC",
            fiscalNumber = "27679970000111",
            address = Address("Test", "test", "test", "test")
        ))
        importerOrderTwo = importerRepository.save(Importer(
            plant = "2195",
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
        var order = Order(
            origin = "USA",
            destination = "Brazil",
            exporter = exporterOrderOne!!,
            number = "4201010101",
            importer = importerOrderOne!!,
            date = LocalDate.now(),
            responsible = user!!,
            modal = Modal.SEA,
            necessity = LocalDate.now(),
            deadline = LocalDate.now()
        )
        order.includeItems(listOf(Item(BigDecimal.TEN, material!!, order)))
        orderOne = orderRepository.save(order)
        order = Order(
            origin = "Belgium",
            destination = "USA",
            exporter = exporterOrderTwo!!,
            number = "4201010102",
            importer = importerOrderTwo!!,
            date = LocalDate.now(),
            responsible = user!!,
            modal = Modal.SEA,
            necessity = LocalDate.now(),
            deadline = LocalDate.now()
        )
        order.includeItems(listOf(Item(BigDecimal.TEN, material!!, order)))
        orderTwo = orderRepository.save(order)
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
    fun `should find all orders without filter`() {
        val response = grpcClient.read(
            ReadOrderRequest.newBuilder()
                .setPageable(generatePageable())
                .build()
        )

        assertEquals(2, response.ordersList.size)
    }

    @Test
    fun `should find all orders by destination`() {
        val response = grpcClient.read(
            ReadOrderRequest.newBuilder()
                .setPageable(generatePageable())
                .setDestination("Brazil")
                .build()
        )

        assertEquals(1, response.ordersList.size)
    }

    @Test
    fun `should find all orders by origin`() {
        val response = grpcClient.read(
            ReadOrderRequest.newBuilder()
                .setPageable(generatePageable())
                .setOrigin("USA")
                .build()
        )

        assertEquals(1, response.ordersList.size)
    }

    @Test
    fun `should find all orders by number`() {
        val response = grpcClient.read(
            ReadOrderRequest.newBuilder()
                .setPageable(generatePageable())
                .setNumber("4201010101")
                .build()
        )

        assertEquals(1, response.ordersList.size)
    }

    @Test
    fun `should find all orders by exporter`() {
        val response = grpcClient.read(
            ReadOrderRequest.newBuilder()
                .setPageable(generatePageable())
                .setExporterId(exporterOrderOne!!.id.toString())
                .build()
        )

        assertEquals(1, response.ordersList.size)
    }

    @Test
    fun `should find all orders by importer`() {
        val response = grpcClient.read(
            ReadOrderRequest.newBuilder()
                .setPageable(generatePageable())
                .setImporterId(importerOrderOne!!.id.toString())
                .build()
        )

        assertEquals(1, response.ordersList.size)
    }

    private fun generatePageable() = Pageable.newBuilder()
        .setDirection(OrderDirection.DESC)
        .setSize(10)
        .setPage(0)
        .setOrderBy("number")
        .build()

}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
        ReadOrderServiceGrpc.newBlockingStub(channel)
}