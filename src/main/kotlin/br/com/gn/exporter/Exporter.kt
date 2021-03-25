package br.com.gn.exporter

import br.com.gn.address.Address
import java.util.*
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "exporter_code_uk", columnNames = ["code"])
    ]
)
class Exporter(
    @field:NotBlank @field:Size(max = 8) @Column(nullable = false, unique = true, updatable = false) val code: String,
    @field:NotBlank @Column(nullable = false) var name: String,
    @field:NotNull @Enumerated(STRING) @Column(nullable = false) var paymentTerms: PaymentTerms,
    @field:NotNull @Embedded var address: Address,
    @field:NotNull @Enumerated(STRING) @Column(nullable = false) var incoterm: Incoterm
) {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

    fun update(request: UpdateExporterRequest) {
        this.address = request.address.toAddress()
        this.name = request.name
        this.paymentTerms = request.paymentTerms!!
        this.incoterm = request.incoterm!!
    }
}
