package br.com.gn.shared.kafka

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.messaging.annotation.Body
import io.micronaut.validation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Validated
@KafkaClient
interface Producer {

    @Topic("routes")
    fun routeNotification(@KafkaKey name: String?, @Valid @Body message: RouteMessage?)

    @Topic("exporters")
    fun exporterNotification(@KafkaKey code: String?, @NotBlank @Body name: String?)
}