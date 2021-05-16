package ch.chaosconnect.rohan.services

import ch.chaosconnect.rohan.assertThrowsWithMessage
import ch.chaosconnect.rohan.meta.StorageConfig
import ch.chaosconnect.rohan.model.RegularUser
import ch.chaosconnect.rohan.runSignedIn
import ch.chaosconnect.rohan.runSignedOut
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream
import javax.security.auth.login.AccountException
import javax.security.auth.login.FailedLoginException

internal class UserServiceImplTest {

    private lateinit var storage: StorageService
    private lateinit var service: UserService

    @BeforeEach
    fun setUp() {
        storage = StorageServiceImpl(StorageConfig())
        service = UserServiceImpl(storage)
    }

    @ParameterizedTest
    @ArgumentsSource(BlankStringProvider::class)
    fun `signUpAsRegularUser does not accept blank name`(name: String) =
        runSignedOut {
            assertThrowsWithMessage<IllegalArgumentException>("User name may not be blank") {
                service.signUpAsRegularUser(name, "123", "Bob89")
            }
        }

    @ParameterizedTest
    @ArgumentsSource(BlankStringProvider::class)
    fun `signUpAsRegularUser does not accept blank password`(password: String) =
        runSignedOut {
            assertThrowsWithMessage<IllegalArgumentException>("Password may not be blank") {
                service.signUpAsRegularUser("löli", password, "Bob89")
            }
        }

    @ParameterizedTest
    @ArgumentsSource(BlankStringProvider::class)
    fun `signUpAsRegularUser does not accept blank display name`(displayName: String) =
        runSignedOut {
            assertThrowsWithMessage<IllegalArgumentException>("Display name may not be blank") {
                service.signUpAsRegularUser("löli", "123", displayName)
            }
        }

    @Test
    fun `signUpAsRegularUser accepts new signed out users`() = runSignedOut {
        service.signUpAsRegularUser("bob", "123", "Bob89")
    }

    @Test
    fun `signUpAsRegularUser throws for signed in regular users`() =
        runSignedInAsRegularUser("bob", "pw", "Bob89") {
            assertThrowsWithMessage<IllegalStateException>("Already signed in as a regular user") {
                service.signUpAsRegularUser("alice", "pw", "Alice90")
            }
        }

    @Test
    fun `signUpAsRegularUser accepts signed in temporary users (upgrade)`() {
        val displayName = "Bob89"
        runSignedInAsTemporaryUser(displayName) {
            service.signUpAsRegularUser("bob", "pw", displayName)
        }
    }

    @Test
    fun `signUpAsRegularUser accepts duplicate display names among regular users`() {
        runSignedOut {
            service.signUpAsRegularUser("eve1", "pw", "Eve89")
            service.signUpAsRegularUser("eve2", "pw", "Eve89")
        }
    }

    @Test
    fun `signUpAsRegularUser accepts duplicate display names among regular and temporary users`() {
        runSignedOut {
            service.signUpAsTemporaryUser("Eve89")
            service.signUpAsRegularUser("eve2", "pw", "Eve89")
        }
    }

    @ParameterizedTest
    @ArgumentsSource(BlankStringProvider::class)
    fun `signInAsTemporaryUser does not accept blank display name`(displayName: String) =
        runSignedOut {
            assertThrowsWithMessage<IllegalArgumentException>("Display name may not be blank") {
                service.signUpAsTemporaryUser(displayName)
            }
        }

    @Test
    fun `signInAsTemporaryUser accepts duplicate display names among temporary users`() =
        runSignedOut {
            service.signUpAsTemporaryUser("Eve89")
            service.signUpAsTemporaryUser("Eve89")
        }

    @Test
    fun `signInAsTemporaryUser accepts duplicate display names among temporary and regular user`() =
        runSignedOut {
            service.signUpAsRegularUser("eve", "pw", "Eve89")
            service.signUpAsTemporaryUser("Eve89")
        }

    @Test
    fun `signInAsRegularUser throws sign-in failure for missing users`() =
        runSignedOut {
            assertThrowsWithMessage<FailedLoginException>("Sign-in failed") {
                service.signInAsRegularUser("Bob", "123")
            }
        }

    @Test
    fun `signInAsRegularUser throws sign-in failure for bad passwords`() =
        runSignedOut {
            val name = "bob"
            val displayName = "Bob89"
            service.signUpAsRegularUser(name, "opw", displayName)
            assertThrowsWithMessage<FailedLoginException>("Sign-in failed") {
                service.signInAsRegularUser("Bob", "npw")
            }
        }

    @Test
    fun `setPassword throws for signed out users`() = runSignedOut {
        assertThrowsWithMessage<IllegalStateException>("No active user") {
            service.setPassword("pw")
        }
    }

    @Test
    fun `setPassword throws for signed in temporary users`() =
        runSignedInAsTemporaryUser("Bob89") {
            assertThrowsWithMessage<AccountException>("Cannot set password for temporary user") {
                service.setPassword("pw")
            }
        }

    @Test
    fun `setPassword invalidates old password for signed in regular users`() {
        val name = "bob"
        val oldPassword = "opw"
        runSignedInAsRegularUser(name, oldPassword, "Bob89") {
            service.setPassword("npw")
        }
        assertThrowsWithMessage<FailedLoginException>("Sign-in failed") {
            runSignedOut {
                service.signInAsRegularUser(name, oldPassword)
            }
        }
    }

    @Test
    fun `setPassword validates new password for signed in regular users`() {
        val name = "bob"
        val newPassword = "npw"
        runSignedInAsRegularUser(name, "opw", "Bob89") {
            service.setPassword(newPassword)
        }
        runSignedOut {
            service.signInAsRegularUser(name, newPassword)
        }
    }

    @Test
    fun `setDisplayName throws for signed out users`() = runSignedOut {
        assertThrowsWithMessage<IllegalStateException>("No active user") {
            service.setDisplayName("Bob89")
        }
    }

    @Test
    fun `getCurrentUser throws for signed out users`() = runSignedOut {
        assertThrowsWithMessage<IllegalStateException>("No active user") {
            service.getCurrentUser()
        }
    }

    @Test
    fun `getCurrentUser throws for user ID with no matching storage entry`() = runSignedIn("loeli") {
        assertThrowsWithMessage<AccountException>("Provided user ID no longer exists") {
            service.getCurrentUser()
        }
    }

    @Test
    fun `getCurrentUser returns current user if signed in`() = runSignedInAsRegularUser("loeli", "123", "Löli") {
        val currentUser = service.getCurrentUser()
        assertTrue(currentUser is RegularUser)
        assertEquals("loeli", (currentUser as RegularUser).credentials.name)
    }

    private fun <T> runSignedInAsTemporaryUser(
        displayName: String,
        block: suspend CoroutineScope.() -> T
    ) =
        runSignedOut {
            runSignedIn(
                service.signUpAsTemporaryUser(displayName).identifier,
                block
            )
        }

    private fun <T> runSignedInAsRegularUser(
        name: String,
        password: String,
        displayName: String,
        block: suspend CoroutineScope.() -> T
    ) = runSignedOut {
        runSignedIn(
            signUpAndInAsRegularUser(name, password, displayName),
            block
        )
    }

    private suspend fun signUpAndInAsRegularUser(
        name: String,
        password: String,
        displayName: String
    ): String {
        val identifier1 =
            service.signUpAsRegularUser(name, password, displayName).identifier
        val identifier2 =
            service.signInAsRegularUser(name, password).identifier
        assertEquals(identifier1, identifier2)
        return identifier2
    }

    private class BlankStringProvider : ArgumentsProvider {
        override fun provideArguments(extensionContext: ExtensionContext): Stream<out Arguments> =
            Stream.of(Arguments.of(""), Arguments.of(" "), Arguments.of("\t"))
    }
}
