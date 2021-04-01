package br.com.gn.material

import br.com.gn.NewMaterialRequest
import br.com.gn.ReadMaterialRequest
import br.com.gn.UpdateMaterialRequest
import br.com.gn.utils.toBigDecimal
import br.com.gn.utils.toPageable
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
        unitPrice = unitPrice.toBigDecimal(),
        pricerPerThousand = pricePerThousand,
        preShipmentLicense = preShipmentLicense,
        planning = planning
    )
}

fun ReadMaterialRequest.toRequestModel(): ReadRequest {
    val pageable = pageable.toPageable()

    return ReadRequest(
        filter = SearchMaterialFilter.from(searchMaterialCase),
        pageable = pageable,
        code = code,
        ncm = ncm,
        description = description
    )
}