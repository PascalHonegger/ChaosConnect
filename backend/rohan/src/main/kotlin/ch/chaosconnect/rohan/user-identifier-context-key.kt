package ch.chaosconnect.rohan

import io.grpc.Context

val userIdentifierContextKey: Context.Key<String> =
    Context.key("UserIdentifier")
