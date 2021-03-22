package br.com.gn.exporter

import br.com.gn.address.AddressRequest
import io.micronaut.core.annotation.Introspected
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class NewExporterRequest(
    @field:NotBlank @field:Size(max = 8) val code: String,
    @field:NotBlank val name: String,
    @field:NotNull val paymentTerms: PaymentTerms,
    @field:NotNull @field:Valid val address: AddressRequest,
    @field:NotNull val incoterm: Incoterm
) {
    fun toModel(): Exporter {
        return Exporter(
            code, name, paymentTerms, address.toAddress(), incoterm
        )
    }
}

@Introspected
data class UpdateExporterRequest(
    @field:NotBlank val name: String,
    @field:NotNull val paymentTerms: PaymentTerms,
    @field:NotNull @field:Valid val address: AddressRequest,
    @field:NotNull val incoterm: Incoterm
)
