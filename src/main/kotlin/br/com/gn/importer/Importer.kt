package br.com.gn.importer

import br.com.gn.address.Address
import org.hibernate.validator.constraints.br.CNPJ
import java.util.*
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "importer_plant_uk", columnNames = ["plant"])
    ]
)
class Importer(
    @field:NotBlank @field:Size(max = 4) @Column(nullable = false, unique = true, updatable = false) val plant: String,
    @field:NotBlank @Column(nullable = false, updatable = false) val fiscalName: String,
    @field:NotBlank @Column(nullable = false, updatable = false) @field:CNPJ val fiscalNumber: String,
    @field:NotNull @field:Valid @Embedded var address: Address
) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

    fun update(request: UpdateImporterRequest) {
        this.address = request.address.toAddress()
    }
}
