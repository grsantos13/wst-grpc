package br.com.gn.order.event

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface EventRepository : JpaRepository<Event, UUID> {
}