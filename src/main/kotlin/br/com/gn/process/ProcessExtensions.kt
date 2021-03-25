package br.com.gn.process

import br.com.gn.NewProcessRequest
import br.com.gn.UpdateProcessRequest
import br.com.gn.process.NewProcessRequest as Request
import br.com.gn.process.UpdateProcessRequest as UpdateRequest

fun NewProcessRequest.toRequestModel(): Request {
    return Request(
        responsibleId = responsibleId,
        name = name,
        operationId = operationId
    )
}

fun UpdateProcessRequest.toRequestModel(): UpdateRequest {
    return UpdateRequest(responsibleId)
}