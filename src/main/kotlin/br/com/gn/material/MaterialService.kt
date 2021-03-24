package br.com.gn.material

import br.com.gn.client.NcmSearchRequest
import br.com.gn.client.NcmSiscomex
import br.com.gn.material.SearchMaterialFilter.CODE
import br.com.gn.material.SearchMaterialFilter.DESCRIPTION
import br.com.gn.material.SearchMaterialFilter.NCM
import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import io.micronaut.data.model.Page
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class MaterialService(
    private val repository: MaterialRepository,
    private val ncmSiscomex: NcmSiscomex
) {

    @Transactional
    fun create(request: NewMaterialRequest): Material {
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
    fun update(request: UpdateMaterialRequest, id: String): Material {
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
    fun delete(id: String): Material {
        val material = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("Material not found for id $id") }

        repository.delete(material)
        return material
    }
}