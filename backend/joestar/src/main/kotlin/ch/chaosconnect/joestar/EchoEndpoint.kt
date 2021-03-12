package ch.chaosconnect.joestar

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Singleton

@Singleton
class EchoEndpoint(private val echoService: EchoService) :
    EchoServiceGrpcKt.EchoServiceCoroutineImplBase() {
    override suspend fun echo(request: EchoRequest): EchoResponse =
        EchoResponse
            .newBuilder()
            .setMessage(echoService.echoViaRohan(request.message))
            .build()

    override fun serverStreamingEcho(request: ServerStreamingEchoRequest): Flow<ServerStreamingEchoResponse> =
        flow {
            repeat(request.messageCount) {
                delay(request.messageInterval.toLong())
                val message = echoService.echo(request.message)
                emit(
                    ServerStreamingEchoResponse
                        .newBuilder()
                        .setMessage(message)
                        .build()
                )
            }
        }
}