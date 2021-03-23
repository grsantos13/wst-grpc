package br.com.gn.deliveryplace

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class NewDeliveryPlaceRequest(
    @field:NotBlank val name: String
) {
    fun toModel(): DeliveryPlace {
        return DeliveryPlace(name)
    }
}