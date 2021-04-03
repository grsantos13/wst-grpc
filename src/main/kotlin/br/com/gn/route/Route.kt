package br.com.gn.route

import br.com.gn.operation.OperationType
import br.com.gn.route.point.RoutePoint
import java.util.*
import javax.persistence.*
import javax.persistence.EnumType.STRING
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class Route(
    @field:NotBlank @Column(nullable = false) val name: String,
    @field:NotNull @Column(nullable = false) @Enumerated(STRING) val operationType: OperationType,
    @field:NotBlank @Column(nullable = false) val origin: String,
    @field:NotBlank @Column(nullable = false) val destination: String,
    @field:NotBlank @Column(nullable = false) val exporter: String,
    @field:NotBlank @Column(nullable = false) val importer: String,
    @field:NotNull @field:Valid @field:Size(min = 1) val points: List<RoutePoint>
) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

}
