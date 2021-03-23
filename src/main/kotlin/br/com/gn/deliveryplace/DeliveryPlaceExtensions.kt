package br.com.gn.deliveryplace

import br.com.gn.NewDeliveryPlaceRequest

fun NewDeliveryPlaceRequest.toModel(): DeliveryPlace {
    return DeliveryPlace(name)
}