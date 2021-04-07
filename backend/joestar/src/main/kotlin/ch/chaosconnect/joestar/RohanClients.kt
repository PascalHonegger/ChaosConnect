package ch.chaosconnect.joestar

import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
internal class Clients {
    @Singleton
    fun gameService(
        @GrpcChannel("rohan") channel: ManagedChannel
    ): GameServiceGrpcKt.GameServiceCoroutineStub {
        return GameServiceGrpcKt.GameServiceCoroutineStub(
            channel
        )
    }
}