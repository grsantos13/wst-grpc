package br.com.gn.order.exporter

import br.com.gn.OrderResponse.ExporterResponse
import io.micronaut.core.annotation.Introspected
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size


@Introspected
@Embeddable
class Exporter(
    @field:NotBlank @field:Size(max = 8) @Column(nullable = false, updatable = false) val code: String,
    @field:NotBlank @Column(nullable = false) var name: String
) {

    fun toGrpcExporterResponse(): ExporterResponse {
        return ExporterResponse.newBuilder()
            .setCode(code)
            .setName(name)
            .build()
    }
}

@Introspected
data class ExporterRequest(
    @field:NotBlank @field:Size(max = 8) val code: String?,
    @field:NotBlank val name: String?
) {
    fun toModel(): Exporter {
        return Exporter(code!!, name!!)
    }
}
