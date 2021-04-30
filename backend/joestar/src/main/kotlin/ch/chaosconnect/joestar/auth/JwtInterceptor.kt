package ch.chaosconnect.joestar.auth

import ch.chaosconnect.api.joestar.WebLoginServiceGrpc
import io.grpc.*
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import javax.inject.Singleton


private val authorizationHeader = Metadata.Key.of(
    "Authorization",
    Metadata.ASCII_STRING_MARSHALLER
)

private val userIdentifierHeader = Metadata.Key.of(
    "UserIdentifier",
    Metadata.ASCII_STRING_MARSHALLER
)

private const val bearerPrefix = "Bearer"

val currentUserIdentifier: Context.Key<String> = Context.key("UserIdentifier")

@Singleton
class JwtServerInterceptor(private val tokenService: TokenService) :
    ServerInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        var context = Context.current()

        // All calls except for Web Login have to be authenticated
        val authRequired =
            call.methodDescriptor.serviceName != WebLoginServiceGrpc.SERVICE_NAME
        val authorizationHeader = headers.get(authorizationHeader)

        if (authorizationHeader == null ||
            !authorizationHeader.startsWith(bearerPrefix)
        ) {
            if (authRequired) {
                throw Status.UNAUTHENTICATED.withDescription("Authorization token is missing or invalid")
                    .asException()
            }
        } else {
            val jwtToken =
                authorizationHeader.substring(bearerPrefix.length).trim()
            val parsed = tokenService.parseToken(jwtToken)
            if (parsed == null) {
                if (authRequired) {
                    throw Status.UNAUTHENTICATED.withDescription("Authorization token was not successfully verified")
                        .asException()
                }
            } else {
                context = context.withValue(currentUserIdentifier, parsed)
            }
        }
        return Contexts.interceptCall(context, call, headers, next)
    }
}

@Singleton
class JwtClientInterceptor : ClientInterceptor {

    override fun <ReqT : Any, RespT : Any> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return object : SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(
                responseListener: Listener<RespT>,
                headers: Metadata
            ) {
                val currentUser = currentUserIdentifier.get()
                if (currentUser != null) {
                    headers.put(userIdentifierHeader, currentUser)
                }
                super.start(responseListener, headers)
            }
        }
    }
}
