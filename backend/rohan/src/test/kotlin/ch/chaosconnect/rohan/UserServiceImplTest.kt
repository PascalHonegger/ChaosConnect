package ch.chaosconnect.rohan

import io.grpc.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UserServiceImplTest {

    private lateinit var service: UserService

    @BeforeEach
    fun setUp() {
        service = UserServiceImpl()
    }

    @Test
    fun `signUpAsRegularUser accepts new signed out users`() {
        runSignedOut {
            service.signUpAsRegularUser("bob", "123", "Bob89")
        }
    }

    @Test
    fun `signUpAsRegularUser accepts new signed out users with unique names`() {
        runSignedOut {
            service.signUpAsRegularUser("bob", "pw", "Bob89")
            service.signUpAsRegularUser("alice", "pw", "Alice90")
        }
    }

    @Test
    fun `signUpAsRegularUser throws when new signed out users use existing names`() {
        runSignedOut {
            service.signUpAsRegularUser("bob", "pw", "Bob89")
            assertThrowsWithMessage<IllegalStateException>("User name 'bob' already in use") {
                service.signUpAsRegularUser("bob", "pw", "Bob90")
            }
        }
    }

    @Test
    fun `signUpAsRegularUser throws for signed in regular users`() {
        runSignedInAsRegularUser("bob", "pw", "Bob89") {
            assertThrowsWithMessage<IllegalStateException>("Already signed in as a regular user") {
                service.signUpAsRegularUser("alice", "pw", "Alice90")
            }
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
        runBlocking {
            service.signUpAsRegularUser("eve1", "pw", "Eve89")
            service.signUpAsRegularUser("eve2", "pw", "Eve89")
        }
    }

    @Test
    fun `signUpAsRegularUser accepts duplicate display names among regular and temporary users`() {
        runBlocking {
            service.signInAsTemporaryUser("Eve89")
            service.signUpAsRegularUser("eve2", "pw", "Eve89")
        }
    }

    @Test
    fun `signInAsTemporaryUser accepts duplicate display names among temporary users`() {
        runBlocking {
            service.signInAsTemporaryUser("Eve89")
            service.signInAsTemporaryUser("Eve89")
        }
    }

    @Test
    fun `signInAsTemporaryUser accepts duplicate display names among temporary and regular user`() {
        runBlocking {
            service.signUpAsRegularUser("eve", "pw", "Eve89")
            service.signInAsTemporaryUser("Eve89")
        }
    }

    @Test
    fun `signInAsRegularUser throws sign-in failure for missing users`() {
        assertThrowsWithMessage<IllegalAccessException>("Sign-in failed") {
            runBlocking {
                service.signInAsRegularUser("Bob", "123")
            }
        }
    }

    @Test
    fun `signInAsRegularUser throws sign-in failure for bad passwords`() {
        val name = "bob"
        val displayName = "Bob89"
        runBlocking {
            service.signUpAsRegularUser(name, "opw", displayName)
        }
        assertThrowsWithMessage<IllegalAccessException>("Sign-in failed") {
            runBlocking {
                service.signInAsRegularUser("Bob", "npw")
            }
        }
    }

    @Test
    fun `setPassword throws for signed out users`() =
        assertThrowsWithMessage<IllegalAccessException>("No active user") {
            runSignedOut {
                service.setPassword("pw")
            }
        }

    @Test
    fun `setPassword throws for signed in temporary users`() =
        assertThrowsWithMessage<IllegalAccessException>("Cannot set password for temporary user") {
            runSignedInAsTemporaryUser("Bob89") {
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
        assertThrowsWithMessage<IllegalAccessException>("Sign-in failed") {
            runBlocking {
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
        runBlocking {
            service.signInAsRegularUser(name, newPassword)
        }
    }

    @Test
    fun `setDisplayName throws for signed out users`() =
        assertThrowsWithMessage<IllegalAccessException>("No active user") {
            runSignedOut {
                service.setDisplayName("Bob89")
            }
        }

    private fun <T> runSignedInAsTemporaryUser(
        displayName: String,
        block: suspend CoroutineScope.() -> T
    ) =
        runBlocking {
            runSignedIn(
                service.signInAsTemporaryUser(displayName),
                block
            )
        }

    private fun <T> runSignedInAsRegularUser(
        name: String,
        password: String,
        displayName: String,
        block: suspend CoroutineScope.() -> T
    ) {
        runBlocking {
            runSignedIn(
                signUpAndInAsRegularUser(name, password, displayName),
                block
            )
        }
    }

    private suspend fun signUpAndInAsRegularUser(
        name: String,
        password: String,
        displayName: String
    ): String {
        val identifier1 =
            service.signUpAsRegularUser(name, password, displayName)
        val identifier2 =
            service.signInAsRegularUser(name, password)
        assertEquals(identifier1, identifier2)
        return identifier2
    }

    companion object {

        private inline fun <reified T : Throwable> assertThrowsWithMessage(
            message: String,
            executable: () -> Unit
        ) =
            assertEquals(message, assertThrows<T>(executable).message)
    }
}
