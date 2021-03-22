package br.com.gn.importer

import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import br.com.gn.shared.validation.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class ImporterService(
    private val repository: ImporterRepository
) {

    @Transactional
    fun create(@Valid request: NewImporterRequest): Importer {
        val existsByPlant = repository.existsByPlant(request.plant)
        if (existsByPlant)
            throw ObjectAlreadyExistsException("Importer already exists with plant ${request.plant}")

        val importer = request.toModel()
        repository.save(importer)
        return importer
    }

    @Transactional
    fun read(plant: String): List<Importer> {
        return when {
            plant.isNullOrBlank() -> repository.findAll()
            else -> repository.findByPlant(plant)
        }
    }

    @Transactional
    fun update(@Valid request: UpdateImporterRequest, @ValidUUID id: String): Importer {
        val importer = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("Importer not found with id $id") }

        importer.update(request)
        return importer
    }

    @Transactional
    fun delete(@ValidUUID id: String): Importer {
        val importer = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("Importer not found with id $id") }

        repository.delete(importer)
        return importer
    }


}