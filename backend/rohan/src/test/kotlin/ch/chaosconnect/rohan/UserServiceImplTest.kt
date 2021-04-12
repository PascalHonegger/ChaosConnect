package ch.chaosconnect.rohan

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
            service.getUser("Bob", "123")
        }
    }

    @Test
    fun getUser_throwsNoSuchElementExceptionWithoutSpecificUsers() {
        service.addUser("Alice", "456", "Alice89")
        assertThrows(NoSuchElementException::class.java) {
            service.getUser("Bob", "123")
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
    ) {
        service.addUser(username, password, displayName)
        assertEquals(displayName, service.getUser(username, password))
    }

    @Test
    fun addUser_throwsIllegalStateExceptionForDuplicateUserNames() {
        service.addUser("Bob", "123", "Bob89")
        assertThrows(IllegalStateException::class.java) {
            service.addUser("Bob", "456", "Bob90")
        }
    }

    @Test
    fun addUser_throwsIllegalStateExceptionForDuplicateDisplayNames() {
        service.addUser("Bob", "123", "Eve")
        assertThrows(IllegalStateException::class.java) {
            service.addUser("Alice", "456", "Eve")
        }
    }

    @Test
    fun addTemporaryUser_throwsIllegalStateExceptionForDuplicateDisplayNamesWithOtherRegularUser() {
        service.addUser("Bob", "123", "Eve")
        assertThrows(IllegalStateException::class.java) {
            service.addTemporaryUser("Eve")
        }
    }

    @Test
    fun addTemporaryUser_throwsIllegalStateExceptionForDuplicateDisplayNamesWithOtherTemporaryUser() {
        service.addTemporaryUser("Eve")
        assertThrows(IllegalStateException::class.java) {
            service.addTemporaryUser("Eve")
        }
    }

    @Test
    fun updateUser() {
        //  TODO: Test authentication failure with old password
    }
}
