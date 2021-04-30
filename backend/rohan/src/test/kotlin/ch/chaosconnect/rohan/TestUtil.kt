package ch.chaosconnect.rohan

import io.grpc.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

fun <T> runSignedIn(
    identifier: String,
    block: suspend CoroutineScope.() -> T
): Unit =
    Context
        .ROOT
        .withValue(userIdentifierContextKey, identifier)
        .run {
            runBlocking {
                block()
            }
        }

fun <T> runSignedOut(block: suspend CoroutineScope.() -> T): Unit =
    runBlocking {
        block()
    }
