package br.com.gn.importer

import br.com.gn.NewImporterRequest
import br.com.gn.UpdateImporterRequest
import br.com.gn.address.toRequestModel
import br.com.gn.importer.NewImporterRequest as Request
import br.com.gn.importer.UpdateImporterRequest as UpdateRequest

fun NewImporterRequest.toRequestModel(): Request {
    return Request(
        plant = plant,
        address = address.toRequestModel(),
        fiscalName = fiscalName,
        fiscalNumber = fiscalNumber
    )
}

fun UpdateImporterRequest.toRequestModel(): UpdateRequest {
    return UpdateRequest(
        address = address.toRequestModel()
    )
}
