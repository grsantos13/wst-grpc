package br.com.gn.order

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun existsByNumber(number: String): Boolean
}