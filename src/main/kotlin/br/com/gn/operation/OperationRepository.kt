package br.com.gn.operation

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface OperationRepository : JpaRepository<Operation, UUID> {
    fun existsByCountryAndType(country: String, type: OperationType): Boolean
}