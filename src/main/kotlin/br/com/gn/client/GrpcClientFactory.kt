package br.com.gn.client

import br.com.gn.RouteServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class GrpcClientFactory {

    @Singleton
    fun wstRouteReadClientStub(@GrpcChannel("wstRouteRead") channel: ManagedChannel) =
        RouteServiceGrpc.newBlockingStub(channel)
}