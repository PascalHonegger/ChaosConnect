package ch.chaosconnect.rohan

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UserServiceImplTest {

    private lateinit var service: UserService

    @BeforeEach
    fun setUp() {
        service = UserServiceImpl()
    }

    @Test
    fun getUser_throwsNoSuchElementExceptionWithoutUsers() {
        assertThrows(NoSuchElementException::class.java) {
            runBlocking {
                service.getUser("Bob", "123")
            }
        }
    }

    @Test
    fun getUser_throwsNoSuchElementExceptionWithoutSpecificUsers() {
        runBlocking {
            service.addUser("Alice", "456", "Alice89")
        }
        assertThrows(NoSuchElementException::class.java) {
            runBlocking {
                service.getUser("Bob", "123")
            }
        }
    }

    @Test
    fun getUser_returnsUserIfAddedBefore() {
        getUser_returnsUserIfAddedBefore("Bob", "123", "Bob89")
    }

    private fun getUser_returnsUserIfAddedBefore(
        username: String,
        password: String,
        displayName: String
    ) =
        runBlocking {
            service.addUser(username, password, displayName)
            assertEquals(displayName, service.getUser(username, password))
        }

    @Test
    fun addUser_throwsIllegalStateExceptionForDuplicateUserNames() {
        runBlocking {
            service.addUser("Bob", "123", "Bob89")
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                service.addUser("Bob", "456", "Bob90")
            }
        }
    }

    @Test
    fun addUser_throwsIllegalStateExceptionForDuplicateDisplayNames() {
        runBlocking {
            service.addUser("Bob", "123", "Eve")
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                service.addUser("Alice", "456", "Eve")
            }
        }
    }

    @Test
    fun addTemporaryUser_throwsIllegalStateExceptionForDuplicateDisplayNamesWithOtherRegularUser() {
        runBlocking {
            service.addUser("Bob", "123", "Eve")
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                service.addTemporaryUser("Eve")
            }
        }
    }

    @Test
    fun addTemporaryUser_throwsIllegalStateExceptionForDuplicateDisplayNamesWithOtherTemporaryUser() {
        runBlocking {
            service.addTemporaryUser("Eve")
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                service.addTemporaryUser("Eve")
            }
        }
    }

    @Test
    fun updateUser() {
        //  TODO: Test authentication failure with old password
    }
}
