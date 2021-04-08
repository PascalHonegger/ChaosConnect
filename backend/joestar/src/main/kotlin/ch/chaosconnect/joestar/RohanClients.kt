package ch.chaosconnect.joestar

import ch.chaosconnect.api.rohan.GameServiceGrpcKt
import ch.chaosconnect.api.rohan.UserServiceGrpcKt
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

    @Singleton
    fun userService(
        @GrpcChannel("rohan") channel: ManagedChannel
    ): UserServiceGrpcKt.UserServiceCoroutineStub {
        return UserServiceGrpcKt.UserServiceCoroutineStub(
            channel
        )
    }
}