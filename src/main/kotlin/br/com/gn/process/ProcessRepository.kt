package br.com.gn.process

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ProcessRepository : JpaRepository<Process, UUID> {
    fun existsByName(name: String): Boolean
    fun findByName(name: String): List<Process>
}