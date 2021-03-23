package br.com.gn.process

import br.com.gn.operation.Operation
import br.com.gn.user.User
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "process_name_uk", columnNames = ["name"])
    ]
)
class Process(
    @NotNull @Valid responsible: User,
    @field:NotBlank @Column(nullable = false) val name: String,
    @field:NotNull @field:Valid @ManyToOne @JoinColumn(nullable = false) val operation: Operation
) {

    @field:NotNull
    @field:Valid
    @ManyToOne
    @JoinColumn(nullable = false)
    var responsible = responsible
        private set

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null

    fun update(responsible: User) {
        this.responsible = responsible
    }
}