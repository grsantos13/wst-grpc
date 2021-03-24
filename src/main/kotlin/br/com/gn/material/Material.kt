package br.com.gn.material

import java.math.BigDecimal
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "material_code_uk", columnNames = ["code"])
    ]
)
class Material(
    @field:NotBlank @Column(nullable = false, unique = true) val code: String,
    @NotBlank @Size(max = 100) description: String,
    @NotBlank @Size(max = 8) @Pattern(regexp = "[0-9]{8}") ncm: String,
    @NotNull @Positive unitPrice: BigDecimal,
    @NotNull pricerPerThousand: Boolean,
    @NotNull preShipmentLicense: Boolean,
    @NotBlank planning: String,
) {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

    @field:NotBlank
    @field:Size(max = 100)
    @Column(nullable = false, length = 100)
    var description = description
        private set

    @field:NotBlank
    @field:Size(max = 8)
    @field:Pattern(regexp = "[0-9]{8}")
    @Column(nullable = false)
    var ncm = ncm
        private set

    @field:NotNull
    @field:Positive
    @Column(nullable = false)
    var unitPrice = unitPrice
        private set

    @field:NotNull
    @Column(nullable = false)
    var pricerPerThousand = pricerPerThousand
        private set

    @field:NotNull
    @Column(nullable = false)
    var preShipmentLicense = preShipmentLicense
        private set

    @field:NotBlank
    @Column(nullable = false)
    var planning = planning
        private set

    var ncmDescription: String? = null

    fun update(request: UpdateMaterialRequest) {
        this.description = request.description
        this.ncm = request.ncm
        this.planning = request.planning
        this.preShipmentLicense = request.preShipmentLicense
        this.pricerPerThousand = request.pricerPerThousand
        this.unitPrice = request.unitPrice
    }

    fun updateNcmDescription(ncmDescription: String) {
        this.ncmDescription = ncmDescription
    }
}