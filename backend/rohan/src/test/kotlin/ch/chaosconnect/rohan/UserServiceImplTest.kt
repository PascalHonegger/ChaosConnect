package ch.chaosconnect.rohan

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class UserServiceImplTest {

    private lateinit var service: UserService

    @BeforeEach
    fun setUp() {
        service = UserServiceImpl()
    }

    @Test
    fun `signUpAsRegularUser throws IllegalStateException for duplicate user names`() {
        runBlocking {
            service.signUpAsRegularUser("Bob", "123", "Bob89")
        }
        assertThrows<IllegalStateException> {
            runBlocking {
                service.signUpAsRegularUser("Bob", "456", "Bob90")
            }
        }
    }

    @Test
    fun `signUpAsRegularUser throws IllegalStateException for duplicate display names`() {
        runBlocking {
            service.signUpAsRegularUser("Bob", "123", "Eve")
        }
        assertThrows<IllegalStateException> {
            runBlocking {
                service.signUpAsRegularUser("Alice", "456", "Eve")
            }
        }
    }

    @Test
    fun `signInAsTemporaryUser throws IllegalStateException for duplicate display names with other regular user`() {
        runBlocking {
            service.signUpAsRegularUser("Bob", "123", "Eve")
        }
        assertThrows<IllegalStateException> {
            runBlocking {
                service.signInAsTemporaryUser("Eve")
            }
        }
    }

    @Test
    fun `signInAsTemporaryUser throws IllegalStateException for duplicate display names with other temporary user`() {
        runBlocking {
            service.signInAsTemporaryUser("Eve")
        }
        assertThrows<IllegalStateException> {
            runBlocking {
                service.signInAsTemporaryUser("Eve")
            }
        }
    }

    @Test
    fun `signInAsRegularUser throws NoSuchElementException without users`() {
        assertThrows<NoSuchElementException> {
            runBlocking {
                service.signInAsRegularUser("Bob", "123")
            }
        }
    }

    @Test
    fun `signInAsRegularUser throws NoSuchElementException without specific users`() {
        runBlocking {
            service.signUpAsRegularUser("Alice", "456", "Alice89")
        }
        assertThrows<NoSuchElementException> {
            runBlocking {
                service.signInAsRegularUser("Bob", "123")
            }
        }
    }

    @ParameterizedTest
    @CsvSource("Bob,123,Bob89")
    fun `signInAsRegularUser returns user if added before`(
        username: String,
        password: String,
        displayName: String
    ) =
        runBlocking {
            service.signUpAsRegularUser(username, password, displayName)
            assertEquals(
                displayName,
                service.signInAsRegularUser(username, password)
            )
        }

    @Test
    fun `updateUser TODO`() {
        //  TODO: Test authentication failure with old password
    }
}
