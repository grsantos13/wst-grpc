package br.com.gn.deliveryplace

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.constraints.NotBlank

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "delivery_place_name_uk", columnNames = ["name"])
    ]
)
class DeliveryPlace(
    @field:NotBlank @Column(nullable = false) val name: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

}