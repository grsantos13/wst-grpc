package br.com.gn.operation

import br.com.gn.NewOperationRequest

fun NewOperationRequest.toModel(): Operation {
    return Operation(country, OperationType.valueOf(type.name))
}