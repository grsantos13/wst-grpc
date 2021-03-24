package br.com.gn.material

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import java.util.*

@Repository
interface MaterialRepository : JpaRepository<Material, UUID> {
    fun existsByCode(code: String): Boolean
    fun findByCode(code: String, pageable: Pageable): Page<Material>
    fun findByNcm(ncm: String, pageable: Pageable): Page<Material>
    fun findByDescriptionContains(description: String, pageable: Pageable): Page<Material>
}
