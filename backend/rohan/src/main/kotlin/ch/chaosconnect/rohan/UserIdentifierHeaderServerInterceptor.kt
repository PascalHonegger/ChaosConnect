package ch.chaosconnect.rohan

import io.grpc.*
import io.grpc.Metadata.Key.of
import javax.inject.Singleton

private const val headerName = "UserIdentifier"

private val metadataKey: Metadata.Key<String> =
    of(headerName, Metadata.ASCII_STRING_MARSHALLER)

@Singleton
class UserIdentifierHeaderServerInterceptor : ServerInterceptor {

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        return Contexts.interceptCall(
            Context.current().withValue(
                userIdentifierContextKey,
                headers.get(metadataKey)
            ),
            call,
            headers,
            next
        )
    }
}
