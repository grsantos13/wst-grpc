package br.com.gn.order

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun existsByNumber(number: String): Boolean
    fun existsByBrokerReference(brokerReference: String): Boolean
    fun findByDestination(destination: String, pageable: Pageable): Page<Order>
    fun findByExporterId(exporterId: UUID, pageable: Pageable): Page<Order>
    fun findByImporterId(importerId: UUID, pageable: Pageable): Page<Order>
    fun findByOrigin(destination: String, pageable: Pageable): Page<Order>
    fun findByNumber(number: String, pageable: Pageable): Page<Order>
}