package br.com.gn.operation

import br.com.gn.NewOperationRequest
import br.com.gn.utils.toEnum

fun NewOperationRequest.toModel(): Operation {
    return Operation(country, type.name.toEnum<OperationType>())
}