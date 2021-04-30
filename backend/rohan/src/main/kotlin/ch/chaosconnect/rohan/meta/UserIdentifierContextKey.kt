package ch.chaosconnect.rohan.meta

import io.grpc.Context

val userIdentifierContextKey: Context.Key<String?> =
    Context.key("UserIdentifier")
