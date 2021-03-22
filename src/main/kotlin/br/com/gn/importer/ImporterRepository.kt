package br.com.gn.importer

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ImporterRepository : JpaRepository<Importer, UUID> {
    fun existsByPlant(plant: String): Boolean
    fun findByPlant(plant: String): List<Importer>
}