package br.com.gn.alert

import java.util.*
import javax.persistence.*
import javax.persistence.EnumType.STRING
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "alert_target_value_uk", columnNames = ["target", "value"])
    ]
)
class Alert(
    @field:NotNull @Enumerated(STRING) @Column(nullable = false) val type: AlertType,
    @field:NotBlank @Column(nullable = false) val target: String,
    @field:NotBlank @Column(nullable = false) val value: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

}