package br.com.gn.exporter

import br.com.gn.NewExporterRequest
import br.com.gn.UpdateExporterRequest
import br.com.gn.address.toRequestModel
import br.com.gn.exporter.NewExporterRequest as Request
import br.com.gn.exporter.UpdateExporterRequest as UpdateRequest

fun NewExporterRequest.toRequestModel(): Request {
    return Request(
        code = code,
        name = name,
        paymentTerms = when (paymentTerms.name) {
            "UNKNOWN" -> null
            else -> PaymentTerms.valueOf(paymentTerms.name)
        },
        address = address.toRequestModel(),
        incoterm = when (incoterm.name) {
            "UNKNOWN_INCOTERM" -> null
            else -> Incoterm.valueOf(incoterm.name)
        }
    )
}


fun UpdateExporterRequest.toRequestModel(): UpdateRequest {
    return UpdateRequest(
        name = name,
        paymentTerms = when (paymentTerms.name) {
            "UNKNOWN" -> null
            else -> PaymentTerms.valueOf(paymentTerms.name)
        },
        address = address.toRequestModel(),
        incoterm = when (incoterm.name) {
            "UNKNOWN_INCOTERM" -> null
            else -> Incoterm.valueOf(incoterm.name)
        }
    )
}