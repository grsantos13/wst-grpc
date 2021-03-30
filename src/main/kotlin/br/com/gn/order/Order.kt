package br.com.gn.order

import br.com.gn.OrderResponse
import br.com.gn.deliveryplace.DeliveryPlace
import br.com.gn.exporter.Exporter
import br.com.gn.importer.Importer
import br.com.gn.order.Status.PENDING_APPROVAL
import br.com.gn.order.event.Event
import br.com.gn.user.User
import java.time.LocalDate
import java.util.*
import javax.persistence.CascadeType.PERSIST
import javax.persistence.CascadeType.REMOVE
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.FetchType.EAGER
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.AUTO
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.Valid
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Size

@Entity
@Table(
    name = "order_processes",
    uniqueConstraints = [
        UniqueConstraint(name = "order_number_uk", columnNames = ["number"])
    ]
)
class Order(
    @field:NotBlank @Column(nullable = false, updatable = false) val origin: String,
    @field:NotBlank @Column(nullable = false, updatable = false) val destination: String,
    @field:NotNull @field:Valid @ManyToOne @JoinColumn(nullable = false, updatable = false) val exporter: Exporter,
    @field:NotNull @field:Size(max = 10) val number: String,
    @field:NotNull @field:Valid @ManyToOne @JoinColumn(nullable = false, updatable = false) val importer: Importer,
    @field:NotNull @field:PastOrPresent @Column(nullable = false, updatable = false) val date: LocalDate,
    @field:NotNull @field:Valid @ManyToOne @JoinColumn(nullable = false) val responsible: User,
    @field:NotNull @Enumerated(STRING) @Column(nullable = false) val modal: Modal,
    @field:NotNull @field:FutureOrPresent @Column(nullable = false) val necessity: LocalDate,
    @field:NotNull @Column(nullable = false) val deadline: LocalDate,
    @Column(length = 1000) val observation: String? = null,
    @ManyToOne val deliveryPlace: DeliveryPlace? = null
) {


    @Id
    @GeneratedValue(strategy = AUTO)
    val id: UUID? = null

    @field:NotNull
    @Enumerated(STRING)
    @Column(nullable = false)
    val status = PENDING_APPROVAL

    @Column(length = 20)
    val brokerReference: String? = null

    @field:NotNull
    @field:Valid
    @field:Size(min = 1)
    @OneToMany(mappedBy = "order", fetch = EAGER)
    var items: List<Item>? = null

    @field:Valid
    @field:NotNull
    @OneToOne(mappedBy = "order", cascade = [PERSIST, REMOVE], fetch = EAGER)
    val event = Event(this)

    fun includeItems(items: List<Item>) {
        this.items = items
    }

    fun toGrpcOrderResponse(): OrderResponse {
        return OrderResponse.newBuilder()
            .setOrigin(origin)
            .setDestination(destination)
            .setExporter(exporter.name)
            .addAllItems(items!!.map {
                OrderResponse.ItemResponse.newBuilder()
                    .setId(it.id.toString())
                    .setCode(it.material.code).setDescription(it.material.description)
                    .setQuantity(it.quantity.toString())
                    .build()
            })
            .setNumber(number)
            .setImporter(importer.plant)
            .setDate(date.toString())
            .setResponsible(responsible.name)
            .setModal(br.com.gn.Modal.valueOf(modal.name))
            .setNecessity(necessity.toString())
            .setDeadline(deadline.toString())
            .setObservation(observation ?: "")
            .setDeliveryPlace(deliveryPlace?.name ?: "")
            .setId(id.toString())
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

}