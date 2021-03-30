package br.com.gn.order

import br.com.gn.NewOrderRequest
import br.com.gn.ReadOrderRequest
import br.com.gn.ReadOrderRequest.SearchOrderCase.DESTINATION
import br.com.gn.ReadOrderRequest.SearchOrderCase.EXPORTERID
import br.com.gn.ReadOrderRequest.SearchOrderCase.IMPORTERID
import br.com.gn.ReadOrderRequest.SearchOrderCase.NUMBER
import br.com.gn.ReadOrderRequest.SearchOrderCase.ORIGIN
import br.com.gn.order.read.Filter
import br.com.gn.utils.toBigDecimal
import br.com.gn.utils.toLocalDate
import javax.validation.ConstraintViolationException
import javax.validation.Validator
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
        modal = when (modal.name) {
            "UNKNOWN_MODAL" -> null
            else -> Modal.valueOf(modal.name)
        },
        necessity = date.toLocalDate(),
        deadline = date.toLocalDate(),
        observation = observation,
        deliveryPlaceId = deliveryPlaceId
    )
}

fun ReadOrderRequest.toFilter(validator: Validator): Filter {
    val filter = when (searchOrderCase!!) {
        DESTINATION -> Filter.ByDestination(destination)
        ORIGIN -> Filter.ByOrigin(origin)
        NUMBER -> Filter.ByNumber(number)
        EXPORTERID -> Filter.ByExporter(exporterId)
        IMPORTERID -> Filter.ByImporter(importerId)
        else -> Filter.Neutral()
    }

    val violations = validator.validate(filter)
    if (violations.isNotEmpty())
        throw ConstraintViolationException(violations)

    return filter
}