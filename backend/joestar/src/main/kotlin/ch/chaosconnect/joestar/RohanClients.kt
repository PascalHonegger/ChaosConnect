package ch.chaosconnect.joestar

import ch.chaosconnect.rohan.EchoServiceGrpcKt
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
internal class Clients {
    @Singleton
    fun reactiveStub(
        @GrpcChannel("rohan") channel: ManagedChannel
    ): EchoServiceGrpcKt.EchoServiceCoroutineStub {
        return EchoServiceGrpcKt.EchoServiceCoroutineStub(
            channel
        )
    }
}