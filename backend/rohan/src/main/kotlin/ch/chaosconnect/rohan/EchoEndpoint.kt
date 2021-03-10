package ch.chaosconnect.rohan

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Singleton

@Singleton
class EchoEndpoint :
    EchoServiceGrpcKt.EchoServiceCoroutineImplBase() {
    override suspend fun echo(request: EchoRequest): EchoResponse =
        EchoResponse
            .newBuilder()
            .setMessage("Rohan said ${request.message}")
            .build()

    override fun serverStreamingEcho(request: ServerStreamingEchoRequest): Flow<ServerStreamingEchoResponse> =
        flow {
            repeat(request.messageCount) {
                delay(request.messageInterval.toLong())
                emit(
                    ServerStreamingEchoResponse
                        .newBuilder()
                        .setMessage("Rohan said ${request.message}")
                        .build()
                )
            }
        }
}