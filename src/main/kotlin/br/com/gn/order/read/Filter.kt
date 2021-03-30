package br.com.gn.order.read

import br.com.gn.order.Order
import br.com.gn.order.OrderRepository
import br.com.gn.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filter {
    abstract fun filter(repository: OrderRepository, pageable: Pageable): Page<Order>

    @Introspected
    data class ByDestination(@field:NotBlank val destination: String) : Filter() {
        override fun filter(repository: OrderRepository, pageable: Pageable): Page<Order> {
            return repository.findByDestination(destination, pageable)
        }
    }

    @Introspected
    data class ByOrigin(@field:NotBlank val origin: String) : Filter() {
        override fun filter(repository: OrderRepository, pageable: Pageable): Page<Order> {
            return repository.findByOrigin(origin, pageable)
        }
    }

    @Introspected
    data class ByExporter(@field:NotBlank @field:ValidUUID val exporterId: String) : Filter() {
        override fun filter(repository: OrderRepository, pageable: Pageable): Page<Order> {
            return repository.findByExporterId(UUID.fromString(exporterId), pageable)
        }
    }

    @Introspected
    data class ByImporter(@field:NotBlank @field:ValidUUID val importerId: String) : Filter() {
        override fun filter(repository: OrderRepository, pageable: Pageable): Page<Order> {
            return repository.findByImporterId(UUID.fromString(importerId), pageable)
        }
    }

    @Introspected
    data class ByNumber(@field:NotBlank @field:Size(min = 10) val number: String) : Filter() {
        override fun filter(repository: OrderRepository, pageable: Pageable): Page<Order> {
            return repository.findByNumber(number, pageable)
        }
    }

    @Introspected
    class Neutral() : Filter() {
        override fun filter(repository: OrderRepository, pageable: Pageable): Page<Order> {
            return repository.findAll(pageable)
        }
    }
}