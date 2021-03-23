package br.com.gn.deliveryplace

import br.com.gn.shared.exception.ObjectAlreadyExistsException
import br.com.gn.shared.exception.ObjectNotFoundException
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class DeliveryPlaceService(
    private val repository: DeliveryPlaceRepository
) {

    @Transactional
    fun create(@Valid request: NewDeliveryPlaceRequest): DeliveryPlace {
        val existsByName = repository.existsByName(request.name)
        if (existsByName)
            throw ObjectAlreadyExistsException("Delivery Place already exists with name ${request.name}")

        val deliveryPlace = request.toModel()
        repository.save(deliveryPlace)
        return deliveryPlace
    }

    @Transactional
    fun read(): List<DeliveryPlace> {
        return repository.findAll()
    }

    fun delete(id: String): DeliveryPlace {
        val deliveryPlace = repository.findById(UUID.fromString(id))
            .orElseThrow { ObjectNotFoundException("Delivery Place not found with id $id") }

        repository.delete(deliveryPlace)
        return deliveryPlace
    }


}
