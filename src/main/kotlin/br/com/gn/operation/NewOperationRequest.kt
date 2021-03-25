package br.com.gn.operation

import io.micronaut.core.annotation.Introspected
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class NewOperationRequest(
    @field:NotBlank val country: String,
    @field:NotNull @Enumerated(EnumType.STRING) val type: OperationType
) {
    fun toModel(): Operation {
        return Operation(
            country = country,
            type = type
        )
    }
}