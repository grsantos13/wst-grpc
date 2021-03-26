package br.com.gn.order

import br.com.gn.material.Material
import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Entity
class Item(
    @field:NotNull @field:Positive val quantity: BigDecimal,
    @field:NotNull @field:Valid @ManyToOne @JoinColumn(nullable = false) val material: Material,
    @field:NotNull @field:Valid @ManyToOne @JoinColumn(nullable = false) var order: Order
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
