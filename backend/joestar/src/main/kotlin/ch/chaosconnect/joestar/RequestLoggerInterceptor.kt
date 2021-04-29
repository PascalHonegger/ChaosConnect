package ch.chaosconnect.joestar

import io.grpc.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

private val logger: Logger =
    LoggerFactory.getLogger(RequestLoggerInterceptor::class.java)

@Singleton
class RequestLoggerInterceptor : ServerInterceptor {

    private class ExceptionTranslatingServerCall<ReqT, RespT>(
        private val delegate: ServerCall<ReqT, RespT>
    ) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(delegate) {

        override fun close(status: Status, trailers: Metadata) {
            if (status.isOk) {
                logger.info("${delegate.methodDescriptor.fullMethodName} completed successfully")
                return super.close(status, trailers)
            }
            val cause = status.cause
            var newStatus = status

            logger.error(
                "${delegate.methodDescriptor.fullMethodName} failed with exception",
                cause
            )

            if (status.code == Status.Code.UNKNOWN) {
                val translatedStatus = when (cause) {
                    is IllegalArgumentException -> Status.INVALID_ARGUMENT
                    is IllegalStateException -> Status.FAILED_PRECONDITION
                    else -> Status.UNKNOWN
                }
                newStatus = translatedStatus.withDescription(cause?.message)
                    .withCause(cause)
            }

            super.close(newStatus, trailers)
        }
    }

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> =
        next.startCall(ExceptionTranslatingServerCall(call), headers)
}
