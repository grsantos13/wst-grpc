package br.com.gn.user

import br.com.gn.NewUserRequest
import br.com.gn.ReadUserRequest
import br.com.gn.ReadUserRequest.ConsultaCase.EMAIL
import br.com.gn.ReadUserRequest.ConsultaCase.NAME
import br.com.gn.UpdateUserRequest
import br.com.gn.user.NewUserRequest as Request
import br.com.gn.user.UpdateUserRequest as UpdateRequest

fun NewUserRequest.toRequestModel(): Request {
    return Request(
        email,
        name
    )
}

fun UpdateUserRequest.toRequestModel(): UpdateRequest {
    return UpdateRequest(email)
}