package br.com.gn.client

import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import javax.validation.constraints.NotBlank

@Client("\${siscomex.ncm.url}")
interface NcmSiscomexClient {
    @Post
    fun search(@Body request: NcmSearchRequest): List<NcmSearchResponse>
}

@Introspected
data class NcmSearchRequest(
    @field:NotBlank val criterio: String?,
    val palavraInteira: Boolean = false
)

class NcmSearchResponse(
    val codigo: String?,
    val fimVigencia: Long?,
    val fragmentosEncontrados: List<String>?,
    val inicioVigencia: Long?,
    val nivelHierarquico: String?,
    val nomeExtenso: String?,
    val possuiFilhos: Boolean?
)