package br.com.gn.deliveryplace

import br.com.gn.NewDeliveryPlaceRequest
import br.com.gn.deliveryplace.NewDeliveryPlaceRequest as Request

fun NewDeliveryPlaceRequest.toRequestModel(): Request {
    return Request(
        name
    )
}