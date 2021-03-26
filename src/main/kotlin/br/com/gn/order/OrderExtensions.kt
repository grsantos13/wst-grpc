package br.com.gn.order

import br.com.gn.NewOrderRequest
import br.com.gn.utils.toBigDecimal
import br.com.gn.utils.toLocalDate
import br.com.gn.order.NewOrderRequest as Request

fun NewOrderRequest.toRequestModel(): Request {
    return Request(
        origin = origin,
        destination = destination,
        exporterId = exporterId,
        items = itemsList.map {
            ItemRequest(
                materialId = it.materialId,
                quantity = it.quantity.toBigDecimal()
            )
        },
        number = number,
        importerId = importerId,
        date = date.toLocalDate(),
        responsibleId = responsibleId,
        modal = Modal.valueOf(modal.name),
        necessity = date.toLocalDate(),
        deadline = date.toLocalDate(),
        observation = observation,
        deliveryPlaceId = deliveryPlaceId
    )
}