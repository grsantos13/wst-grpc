package br.com.gn.importer

import br.com.gn.address.AddressRequest
import io.micronaut.core.annotation.Introspected
import org.hibernate.validator.constraints.br.CNPJ
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class NewImporterRequest(
    @field:NotBlank @field:Size(max = 4) val plant: String,
    @field:NotBlank val fiscalName: String,
    @field:NotBlank @field:CNPJ val fiscalNumber: String,
    @field:NotNull @field:Valid val address: AddressRequest
) {
    fun toModel(): Importer {
        return Importer(
            plant = plant,
            address = address.toAddress(),
            fiscalName = fiscalName,
            fiscalNumber = fiscalNumber
        )
    }
}

@Introspected
data class UpdateImporterRequest(
    @field:NotNull @field:Valid val address: AddressRequest
)
