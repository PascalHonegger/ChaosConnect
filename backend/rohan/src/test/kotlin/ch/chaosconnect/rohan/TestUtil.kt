package ch.chaosconnect.rohan

import ch.chaosconnect.rohan.meta.userIdentifierContextKey
import io.grpc.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

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

inline fun <reified T : Throwable> assertThrowsWithMessage(
    message: String,
    executable: () -> Unit
) =
    assertEquals(message, assertThrows<T>(executable).message)
