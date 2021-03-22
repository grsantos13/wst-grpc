package br.com.gn.exporter

import br.com.gn.Address
import br.com.gn.NewExporterRequest
import br.com.gn.UpdateExporterRequest
import br.com.gn.address.AddressRequest
import br.com.gn.exporter.NewExporterRequest as Request
import br.com.gn.exporter.UpdateExporterRequest as UpdateRequest

fun NewExporterRequest.toRequestModel(): Request {
    return Request(
        code = code,
        name = name,
        paymentTerms = PaymentTerms.valueOf(paymentTerms.name),
        address = address.toRequestModel(),
        incoterm = Incoterm.valueOf(incoterm.name)
    )
}


fun UpdateExporterRequest.toRequestModel(): UpdateRequest {
    return UpdateRequest(
        name = name,
        paymentTerms = PaymentTerms.valueOf(paymentTerms.name),
        address = address.toRequestModel(),
        incoterm = Incoterm.valueOf(incoterm.name)
    )
}

fun Address.toRequestModel(): AddressRequest {
    return AddressRequest(
        street = street,
        city = city,
        zipCode = zipCode,
        country = country
    )
}