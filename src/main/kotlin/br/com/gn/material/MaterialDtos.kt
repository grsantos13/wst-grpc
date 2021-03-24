package br.com.gn.material

import br.com.gn.ReadMaterialRequest.SearchMaterialCase
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Pageable
import java.math.BigDecimal
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
data class NewMaterialRequest(
    @field:NotBlank val code: String,
    @field:NotBlank @field:Size(max = 100) val description: String,
    @field:NotBlank @field:Size(max = 10) @field:Pattern(regexp = "[0-9]{8}") val ncm: String,
    @field:NotNull @field:Positive val unitPrice: BigDecimal,
    @field:NotNull val pricerPerThousand: Boolean,
    @field:NotNull val preShipmentLicense: Boolean,
    @field:NotBlank val planning: String,
) {

    fun toModel(): Material {
        return Material(
            code = code,
            description = description,
            ncm = ncm,
            unitPrice = unitPrice,
            pricerPerThousand = pricerPerThousand,
            preShipmentLicense = preShipmentLicense,
            planning = planning
        )
    }
}

@Introspected
data class UpdateMaterialRequest(
    @field:NotBlank @field:Size(max = 100) val description: String,
    @field:NotBlank @field:Size(max = 10) @field:Pattern(regexp = "[0-9]{8}") val ncm: String,
    @field:NotNull @field:Positive val unitPrice: BigDecimal,
    @field:NotNull val pricerPerThousand: Boolean,
    @field:NotNull val preShipmentLicense: Boolean,
    @field:NotBlank val planning: String,
)

data class ReadMaterialRequest(
    val filter: SearchMaterialFilter,
    val pageable: Pageable,
    val code: String?,
    val ncm: String?,
    val description: String?
)

enum class SearchMaterialFilter(val grpcSearch: SearchMaterialCase?) {
    CODE(SearchMaterialCase.CODE),
    NCM(SearchMaterialCase.NCM),
    DESCRIPTION(SearchMaterialCase.DESCRIPTION),
    ELSE(SearchMaterialCase.SEARCHMATERIAL_NOT_SET);

    companion object {
        val filters = SearchMaterialFilter.values().associateBy(SearchMaterialFilter::grpcSearch)

        fun from(grpcSearch: SearchMaterialCase?): SearchMaterialFilter {
            return filters[grpcSearch] ?: ELSE
        }
    }
}