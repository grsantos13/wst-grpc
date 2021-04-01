package br.com.gn.operation

import java.util.*
import javax.persistence.Column
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

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "operation_country_type_uk", columnNames = ["country", "type"])
    ]
)
class Operation(
    @field:NotBlank @Column(nullable = false, updatable = false) val country: String,
    @field:NotNull @Enumerated(STRING) @Column(nullable = false, updatable = false) val type: OperationType?
) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

}