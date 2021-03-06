package br.com.gn.process

import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import br.com.gn.shared.validation.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class ProcessService(
    private val repository: ProcessRepository,
    private val manager: EntityManager
) {

    @Transactional
    fun create(@Valid request: NewProcessRequest): Process {
        if (repository.existsByName(request.name))
            throw ObjectAlreadyExistsException("Process already exists with name ${request.name}")

        val process = request.toModel(manager)
        repository.save(process)
        return process
    }

    @Transactional
    fun read(name: String?): List<Process> {
        return if (name.isNullOrBlank())
            repository.findAll()
        else repository.findByName(name)
    }

    @Transactional
    fun update(@Valid request: UpdateProcessRequest, @NotBlank @ValidUUID id: String): Process {
        val process = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("Process not found with id $id") }

        process.update(request.responsible(manager))
        return process
    }

    @Transactional
    fun delete(@NotBlank @ValidUUID id: String): Process {
        val process = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("Process not found with id $id") }
        repository.delete(process)
        return process
    }
}