package br.com.gn.exporter

import br.com.gn.DeleteExporterRequest
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class ExporterService(
    private val manager: EntityManager
) {

    @Transactional
    fun create(@Valid request: NewExporterRequest): Exporter {
        val exporter = request.toModel()
        manager.persist(exporter)
        return exporter
    }

    @Transactional
    fun read(name: String): List<Exporter> {
        return when {
            name.isNullOrBlank() -> manager.createQuery(" select e from Exporter e ", Exporter::class.java)
                .resultList
            else -> manager.createQuery(" select e from Exporter e where e.name = :name ", Exporter::class.java)
                .setParameter("name", name)
                .resultList
        }
    }

    @Transactional
    fun update(@Valid request: UpdateExporterRequest, id: String): Exporter {
        val exporter = manager.find(Exporter::class.java, UUID.fromString(id))
            ?: throw IllegalArgumentException("Exporter not found with id $id")

        exporter.update(request)

        return exporter
    }

    @Transactional
    fun delete(request: DeleteExporterRequest): Exporter {
        val exporter = manager.find(Exporter::class.java, UUID.fromString(request.id))
            ?: throw IllegalArgumentException("Exporter not found with id ${request.id}")

        manager.remove(exporter)
        return exporter
    }

}