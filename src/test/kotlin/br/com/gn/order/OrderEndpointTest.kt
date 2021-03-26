package br.com.gn.order

import br.com.gn.Modal
import br.com.gn.NewOrderRequest
import br.com.gn.OrderServiceGrpc
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

@MicronautTest(transactional = false)
internal class OrderEndpointTest(
    private val grpcClient: OrderServiceGrpc.OrderServiceBlockingStub,
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
    fun `should save a order successfully`() {
        val request = NewOrderRequest.newBuilder()
            .setOrigin("EUA")
            .setDestination("Brazil")
            .setExporterId(exporter!!.id.toString())
            .addItems(
                0, NewOrderRequest.Item.newBuilder()
                    .setMaterialId(material!!.id.toString())
                    .setQuantity("1000")
                    .build()
            )
            .setNumber("4200212121")
            .setImporterId(importer!!.id.toString())
            .setDate(LocalDate.now().toString())
            .setResponsibleId(user!!.id.toString())
            .setModal(Modal.SEA)
            .setNecessity(LocalDate.now().toString())
            .setDeadline(LocalDate.now().toString())
            .setObservation("Test")
            .setDeliveryPlaceId(deliveryPlace!!.id.toString())
            .build()

        val response = grpcClient.create(request)
        assertNotNull(response.id)
    }
}

@Factory
class Client {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) =
        OrderServiceGrpc.newBlockingStub(channel)
}