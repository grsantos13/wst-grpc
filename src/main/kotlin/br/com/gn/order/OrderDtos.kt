package br.com.gn.order

import br.com.gn.deliveryplace.DeliveryPlace
import br.com.gn.exporter.Exporter
import br.com.gn.importer.Importer
import br.com.gn.material.Material
import br.com.gn.shared.exception.ObjectNotFoundException
import br.com.gn.shared.validation.ValidUUID
import br.com.gn.user.User
import br.com.gn.utils.toLocalDate
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import javax.persistence.EntityManager
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
data class NewOrderRequest(
    @field:NotBlank val origin: String,
    @field:NotBlank val destination: String,
    @field:NotBlank @field:ValidUUID val exporterId: String,
    @field:NotNull @field:Valid @field:Size(min = 1) val items: List<ItemRequest>,
    @field:NotNull @field:Size(max = 10) val number: String,
    @field:NotBlank @field:ValidUUID val importerId: String,
    @field:NotNull @field:PastOrPresent val date: LocalDate?,
    @field:NotBlank @field:ValidUUID val responsibleId: String,
    @field:NotNull val modal: Modal?,
    @field:NotNull val necessity: LocalDate?,
    @field:NotNull val deadline: LocalDate?,
    @field:NotBlank @field:Size(max = 1000) val observation: String? = null,
    @field:NotBlank @field:ValidUUID val deliveryPlaceId: String? = null,
    val route: String? = null
) {
    fun toModel(manager: EntityManager): Order {
        val exporter = manager.find(Exporter::class.java, UUID.fromString(exporterId))
            ?: throw ObjectNotFoundException("Exporter not found with id $exporterId")

        val importer = manager.find(Importer::class.java, UUID.fromString(importerId))
            ?: throw ObjectNotFoundException("Importer not found with id $importerId")

        val responsible = manager.find(User::class.java, UUID.fromString(responsibleId))
            ?: throw ObjectNotFoundException("User not found with id $responsibleId")

        var deliveryPlace: DeliveryPlace? = null
        if (!deliveryPlaceId.isNullOrBlank())
            deliveryPlace = manager.find(DeliveryPlace::class.java, UUID.fromString(deliveryPlaceId))
                ?: throw ObjectNotFoundException("Delivery place not found with id $deliveryPlaceId")


        val order = Order(
            origin = origin,
            destination = destination,
            exporter = exporter,
            number = number,
            importer = importer,
            date = date!!,
            responsible = responsible,
            modal = modal!!,
            necessity = necessity!!,
            deadline = deadline!!,
            observation = observation,
            deliveryPlace = deliveryPlace,
            route = route
        )

        val items = items.map {
            val material = (manager.find(Material::class.java, UUID.fromString(it.materialId))
                ?: throw ObjectNotFoundException("Material not found with id ${it.materialId}"))
            Item(material = material, quantity = it.quantity!!, order = order)
        }

        order.includeItems(items)

        return order
    }
}

@Introspected
data class ItemRequest(
    @field:NotBlank @field:ValidUUID val materialId: String,
    @field:NotNull @field:Positive val quantity: BigDecimal?
)

@Introspected
data class UpdateOrderRequest(
    @field:ValidUUID val deliveryPlaceId: String? = null,
    @field:NotNull val modal: Modal?,
    @field:NotBlank val necessity: String,
    @field:NotBlank @field:ValidUUID val responsibleId: String,
    @field:NotBlank val deadline: String,
    val route: String? = null
) {
    class UpdateRequest(
        val deliveryPlace: DeliveryPlace? = null,
        @field:NotNull val modal: Modal,
        @field:NotNull val necessity: LocalDate?,
        @field:NotNull val responsible: User,
        @field:NotNull val deadline: LocalDate?,
        val route: String? = null
    )

    fun toUpdateRequest(manager: EntityManager): UpdateRequest {
        var deliveryPlace: DeliveryPlace? = null
        if (deliveryPlaceId != null)
            deliveryPlace = manager.find(DeliveryPlace::class.java, UUID.fromString(deliveryPlaceId))
                ?: throw ObjectNotFoundException("Delivery place not found with id $deliveryPlaceId")

        val responsible = manager.find(User::class.java, UUID.fromString(responsibleId))
            ?: throw ObjectNotFoundException("User not found with id $responsibleId")

        return UpdateRequest(
            deliveryPlace = deliveryPlace,
            modal = modal!!,
            necessity = necessity.toLocalDate(),
            responsible = responsible,
            deadline = deadline.toLocalDate(),
            route = route
        )
    }

}

