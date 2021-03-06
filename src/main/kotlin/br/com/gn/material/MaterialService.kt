package br.com.gn.material

import br.com.gn.client.NcmSearchRequest
import br.com.gn.client.NcmSiscomexClient
import br.com.gn.material.SearchMaterialFilter.CODE
import br.com.gn.material.SearchMaterialFilter.DESCRIPTION
import br.com.gn.material.SearchMaterialFilter.NCM
import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import br.com.gn.shared.validation.ValidUUID
import io.micronaut.data.model.Page
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class MaterialService(
    private val repository: MaterialRepository,
    private val ncmSiscomex: NcmSiscomexClient
) {

    @Transactional
    fun create(@Valid request: NewMaterialRequest): Material {
        if (repository.existsByCode(request.code))
            throw ObjectAlreadyExistsException("Material with code ${request.code} already exists")

        val response = ncmSiscomex.search(NcmSearchRequest(request.ncm))
        if (response.isEmpty())
            throw IllegalArgumentException("Ncm ${request.ncm} not found in Siscomex")

        val material = request.toModel()
        material.updateNcmDescription(response.first().nomeExtenso!!)
        repository.save(material)

        return material
    }

    @Transactional
    fun update(@Valid request: UpdateMaterialRequest, @NotBlank @ValidUUID id: String): Material {
        val material = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("Material not found for id $id") }

        val response = ncmSiscomex.search(NcmSearchRequest(request.ncm))
        if (response.isEmpty())
            throw IllegalArgumentException("Ncm ${request.ncm} not found in Siscomex")

        material.updateNcmDescription(response.first().nomeExtenso!!)
        material.update(request)

        return material
    }

    @Transactional
    fun read(request: ReadMaterialRequest): Page<Material> {
        return when (request.filter) {
            CODE -> repository.findByCode(request.code!!, request.pageable)
            NCM -> repository.findByNcm(request.ncm!!, request.pageable)
            DESCRIPTION -> repository.findByDescriptionContains(request.description!!, request.pageable)
            else -> repository.findAll(request.pageable)
        }
    }

    @Transactional
    fun delete(@NotBlank @ValidUUID id: String): Material {
        val material = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("Material not found for id $id") }

        repository.delete(material)
        return material
    }
}