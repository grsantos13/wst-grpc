package br.com.gn.order

import br.com.gn.NewOrderRequest
import br.com.gn.NewOrderRequest.ExporterRequest
import br.com.gn.ReadOrderRequest
import br.com.gn.ReadOrderRequest.SearchOrderCase.*
import br.com.gn.UpdateOrderRequest
import br.com.gn.order.read.Filter
import br.com.gn.utils.toBigDecimal
import br.com.gn.utils.toEnum
import br.com.gn.utils.toLocalDate
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import br.com.gn.order.NewOrderRequest as Request
import br.com.gn.order.UpdateOrderRequest as UpdateRequest
import br.com.gn.order.exporter.ExporterRequest as ExporterRequestModel


fun NewOrderRequest.toRequestModel(): Request {
    return Request(
        origin = origin,
        destination = destination,
        exporter = exporter.toRequestModel(),
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
        modal = modal.name.toEnum<Modal>(),
        necessity = necessity.toLocalDate(),
        deadline = deadline.toLocalDate(),
        observation = observation,
        deliveryPlace = deliveryPlace,
        route = route
    )
}

fun ReadOrderRequest.toFilter(validator: Validator): Filter {
    val filter = when (searchOrderCase!!) {
        DESTINATION -> Filter.ByDestination(destination)
        ORIGIN -> Filter.ByOrigin(origin)
        NUMBER -> Filter.ByNumber(number)
        EXPORTERCODE -> Filter.ByExporter(exporterCode)
        IMPORTERID -> Filter.ByImporter(importerId)
        else -> Filter.Neutral()
    }

    val violations = validator.validate(filter)
    if (violations.isNotEmpty())
        throw ConstraintViolationException(violations)

    return filter
}

fun UpdateOrderRequest.toRequestModel(): UpdateRequest {
    return UpdateRequest(
        deadline = this.deadline,
        deliveryPlace = this.deliveryPlace,
        modal = modal.name.toEnum<Modal>(),
        necessity = this.necessity,
        responsibleId = this.responsibleId,
        route = route
    )
}

fun ExporterRequest.toRequestModel(): ExporterRequestModel {
    return ExporterRequestModel(
        code, name
    )
}