package br.com.gn.importer

import br.com.gn.DeleteImporterRequest
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class ImporterService(
    private val manager: EntityManager
) {

    @Transactional
    fun create(@Valid request: NewImporterRequest): Importer {
        val importer = request.toModel()
        manager.persist(importer)
        return importer
    }

    @Transactional
    fun read(plant: String): List<Importer> {
        return when {
            plant.isNullOrBlank() -> manager.createQuery(" select i from Importer i ", Importer::class.java)
                .resultList
            else -> manager.createQuery(" select i from Importer i where i.plant = :plant ", Importer::class.java)
                .setParameter("plant", plant)
                .resultList
        }
    }

    @Transactional
    fun update(@Valid request: UpdateImporterRequest, id: String): Importer {
        val importer = manager.find(Importer::class.java, UUID.fromString(id))
            ?: throw IllegalArgumentException("Importer not found with id $id")

        importer.update(request)

        return importer
    }

    @Transactional
    fun delete(request: DeleteImporterRequest): Importer {
        val importer = manager.find(Importer::class.java, UUID.fromString(request.id))
            ?: throw IllegalArgumentException("Importer not found with id ${request.id}")

        manager.remove(importer)
        return importer
    }


}