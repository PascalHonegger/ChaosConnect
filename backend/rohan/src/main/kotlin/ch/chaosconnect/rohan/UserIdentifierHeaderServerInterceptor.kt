package ch.chaosconnect.rohan

import io.grpc.*
import io.grpc.Metadata.Key.of
import javax.inject.Singleton

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
                extractAndValidateUserIdentifier(headers)
            ),
            call,
            headers,
            next
        )
    }

    companion object {

        private const val headerName = "UserIdentifier"

        private val metadataKey: Metadata.Key<String> =
            of(headerName, Metadata.ASCII_STRING_MARSHALLER)

        private fun extractAndValidateUserIdentifier(headers: Metadata): String {
            val userIdentifier = headers.get(metadataKey)
            if (userIdentifier == null) {
                throw Status.UNAUTHENTICATED
                    .withDescription("'$headerName' header not set")
                    .asException()
            } else if (userIdentifier.isEmpty()) {
                throw Status.UNAUTHENTICATED
                    .withDescription("'$headerName' header is empty")
                    .asException()
            }
            return userIdentifier
        }
    }
}
