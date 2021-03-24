package br.com.gn.material

import br.com.gn.NewMaterialRequest
import br.com.gn.ReadMaterialRequest
import br.com.gn.UpdateMaterialRequest
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.data.model.Sort.Order.Direction
import java.math.BigDecimal
import br.com.gn.material.NewMaterialRequest as Request
import br.com.gn.material.ReadMaterialRequest as ReadRequest
import br.com.gn.material.UpdateMaterialRequest as UpdateRequest

fun NewMaterialRequest.toRequestModel(): Request {
    return Request(
        code = code,
        description = description,
        ncm = ncm,
        unitPrice = if (unitPrice.isNotBlank()) BigDecimal(unitPrice) else BigDecimal.ZERO,
        pricerPerThousand = pricePerThousand,
        preShipmentLicense = preShipmentLicense,
        planning = planning
    )
}

fun UpdateMaterialRequest.toRequestModel(): UpdateRequest {
    return UpdateRequest(
        description = description,
        ncm = ncm,
        unitPrice = if (unitPrice.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(unitPrice),
        pricerPerThousand = pricePerThousand,
        preShipmentLicense = preShipmentLicense,
        planning = planning
    )
}

fun ReadMaterialRequest.toRequestModel(): ReadRequest {
    val pageable = with(this.pageable) {
        Pageable.from(page, size, Sort.of(Sort.Order(orderBy, Direction.valueOf(direction.name), true)))
    }
    return ReadRequest(
        filter = SearchMaterialFilter.from(searchMaterialCase),
        pageable = pageable,
        code = code,
        ncm = ncm,
        description = description
    )
}