package br.com.gn.order.event

import br.com.gn.order.Order
import java.time.LocalDate
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "event_order_uk", columnNames = ["order_id"])
    ]
)
class Event(
    @field:NotNull @field:Valid @OneToOne @JoinColumn(name = "order_id") val order: Order
) {

    val availability: LocalDate? = null
    val estimatedDeparture: LocalDate? = null
    val realDeparture: LocalDate? = null
    val estimatedArrival: LocalDate? = null
    val realArrival: LocalDate? = null
    val preAlert: LocalDate? = null
    val wrongNecessityAlert: LocalDate? = null

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null


}
