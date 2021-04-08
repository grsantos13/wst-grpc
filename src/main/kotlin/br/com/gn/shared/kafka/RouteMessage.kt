package br.com.gn.shared.kafka

import br.com.gn.OperationType
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class RouteMessage(
    @field:NotBlank val name: String,
    @field:NotBlank val exporterCode: String,
    @field:NotBlank val importerPlant: String,
    @field:NotNull val type: OperationType
) {
}