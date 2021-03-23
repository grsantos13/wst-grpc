package br.com.gn.deliveryplace

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface DeliveryPlaceRepository : JpaRepository<DeliveryPlace, UUID> {
    fun existsByName(name: String): Boolean
}