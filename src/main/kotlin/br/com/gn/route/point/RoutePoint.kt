package br.com.gn.route.point

import br.com.gn.route.Route
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.PositiveOrZero

@Entity
class RoutePoint(
    @field:NotNull @field:Valid val route: Route,
    @field:NotNull @field:Valid val point: Point,
    @field:NotNull @field:PositiveOrZero val lt: Int
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}
