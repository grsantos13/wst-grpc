package br.com.gn.route.point

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "point_name_uk", columnNames = ["name"])
    ]
)
class Point(
    @field:NotBlank @Column(nullable = false, unique = true) val name: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
