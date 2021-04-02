package br.com.gn.order

import br.com.gn.OrderResponse
import br.com.gn.deliveryplace.DeliveryPlace
import br.com.gn.exporter.Exporter
import br.com.gn.importer.Importer
import br.com.gn.order.Status.PENDING_APPROVAL
import br.com.gn.order.event.Event
import br.com.gn.user.User
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType.PERSIST
import javax.persistence.CascadeType.REMOVE
import javax.persistence.EnumType.STRING
import javax.persistence.FetchType.EAGER
import javax.persistence.GenerationType.AUTO
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Size

@Entity
@Table(
    name = "order_processes",
    uniqueConstraints = [
        UniqueConstraint(name = "order_number_uk", columnNames = ["number"]),
        UniqueConstraint(name = "order_broker_reference_uk", columnNames = ["broker_reference"])
    ]
)
class Order(
    @field:NotBlank @Column(nullable = false, updatable = false) val origin: String,
    @field:NotBlank @Column(nullable = false, updatable = false) val destination: String,
    @field:NotNull @field:Valid @ManyToOne @JoinColumn(nullable = false, updatable = false) val exporter: Exporter,
    @field:NotNull @field:Size(max = 10) val number: String,
    @field:NotNull @field:Valid @ManyToOne @JoinColumn(nullable = false, updatable = false) val importer: Importer,
    @field:NotNull @field:PastOrPresent @Column(nullable = false, updatable = false) val date: LocalDate,
    @NotNull @Valid responsible: User,
    @NotNull modal: Modal,
    @NotNull necessity: LocalDate,
    @NotNull deadline: LocalDate,
    observation: String? = null,
    deliveryPlace: DeliveryPlace? = null
) {


    @Id
    @GeneratedValue(strategy = AUTO)
    val id: UUID? = null

    @field:NotNull
    @Enumerated(STRING)
    @Column(nullable = false)
    val status = PENDING_APPROVAL

    @field:Size(max = 20)
    @Column(length = 20, name = "broker_reference")
    var brokerReference: String? = null

    @field:NotNull
    @field:Valid
    @field:Size(min = 1)
    @OneToMany(mappedBy = "order", fetch = EAGER, cascade = [PERSIST])
    @OnDelete(action = OnDeleteAction.CASCADE)
    var items: List<Item>? = null

    @field:Valid
    @field:NotNull
    @OneToOne(mappedBy = "order", cascade = [PERSIST, REMOVE], fetch = EAGER)
    val event = Event(this)

    @field:NotNull
    @Column(nullable = false)
    var deadline = deadline
        private set

    @field:NotNull
    @Column(nullable = false)
    var necessity = necessity
        private set

    @field:NotNull
    @Enumerated(STRING)
    @Column(nullable = false)
    var modal = modal
        private set

    @field:NotNull
    @field:Valid
    @ManyToOne
    @JoinColumn(nullable = false)
    var responsible = responsible
        private set

    @ManyToOne
    var deliveryPlace = deliveryPlace
        private set

    @Column(length = 1000)
    var observation = observation
        private set

    fun includeItems(items: List<Item>) {
        this.items = items
    }

    fun toGrpcOrderResponse(): OrderResponse {
        return OrderResponse.newBuilder()
            .setOrigin(origin)
            .setDestination(destination)
            .setExporter(exporter.toGrpcExporterResponse())
            .addAllItems(items!!.map {
                OrderResponse.ItemResponse.newBuilder()
                    .setId(it.id.toString())
                    .setCode(it.material.code).setDescription(it.material.description)
                    .setQuantity(it.quantity.toString())
                    .setPreShipmentLicense(it.material.preShipmentLicense)
                    .setNcm(it.material.ncm)
                    .setUnitPrice(it.material.unitPrice.toString())
                    .build()
            })
            .setNumber(number)
            .setImporter(importer.toGrpcImporterResponse())
            .setDate(date.toString())
            .setResponsible(
                OrderResponse.ResponsibleResponse.newBuilder()
                    .setEmail(responsible.email)
                    .setName(responsible.name)
                    .build()
            )
            .setModal(br.com.gn.Modal.valueOf(modal.name))
            .setNecessity(necessity.toString())
            .setDeadline(deadline.toString())
            .setObservation(observation ?: "")
            .setDeliveryPlace(deliveryPlace?.name ?: "")
            .setId(id.toString())
            .setBrokerReference(brokerReference ?: "")
            .setEvents(
                OrderResponse.EventResponse.newBuilder()
                    .setAvailability(event.availability.toString())
                    .setEstimatedDeparture(event.estimatedDeparture.toString())
                    .setRealDeparture(event.realDeparture.toString())
                    .setEstimatedArrival(event.estimatedArrival.toString())
                    .setRealArrival(event.realArrival.toString())
                    .setPreAlert(event.preAlert.toString())
                    .setWrongNecessityAlert(event.wrongNecessityAlert.toString())
                    .setId(event.id.toString())
                    .build()
            )
            .build()
    }

    fun update(updateRequest: UpdateOrderRequest.UpdateRequest) {
        deliveryPlace = updateRequest.deliveryPlace
        modal = updateRequest.modal
        necessity = updateRequest.necessity!!
        responsible = updateRequest.responsible
        deadline = updateRequest.deadline!!
    }

    fun updateObservation(observation: String) {
        this.observation = observation
    }

    fun updateReference(reference: String) {
        this.brokerReference = reference
    }

}