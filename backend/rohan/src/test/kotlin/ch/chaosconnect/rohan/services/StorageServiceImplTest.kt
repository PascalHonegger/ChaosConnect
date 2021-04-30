package ch.chaosconnect.rohan.services

import ch.chaosconnect.rohan.assertThrowsWithMessage
import ch.chaosconnect.rohan.meta.StorageConfig
import ch.chaosconnect.rohan.model.RegularUser
import ch.chaosconnect.rohan.model.TemporaryUser
import ch.chaosconnect.rohan.model.UserCredentials
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
@ExperimentalStdlibApi
internal class StorageServiceImplTest {

    private lateinit var service: StorageService

    @BeforeEach
    fun setUp() {
        service = StorageServiceImpl(StorageConfig())
    }

    @Test
    fun `addUser gives a unique ID every time`() {
        var id1: String? = null
        var id2: String? = null

        service.addUser {
            id1 = it
            TemporaryUser(it, "User1")
        }
        service.addUser {
            id2 = it
            TemporaryUser(it, "User2")
        }

        assertNotNull(id1)
        assertNotNull(id2)
        assertNotEquals(id1, id2)
    }

    @Test
    fun `can add user with duplicate display name`() {
        service.addUser { TemporaryUser(it, "user") }
        service.addUser { TemporaryUser(it, "user") }
    }

    @ParameterizedTest
    @MethodSource("usernames")
    fun `can not add user with duplicate usernames`(
        username1: String,
        username2: String
    ) {
        val cred1 = UserCredentials(username1, "123")
        val cred2 = UserCredentials(username2, "abc")
        service.addUser { RegularUser(it, "User 1", cred1) }
        assertThrowsWithMessage<IllegalStateException>("Username '$username2' already in use") {
            service.addUser { RegularUser(it, "User 2", cred2) }
        }
    }

    @Test
    fun `can change properties of user`() {
        val addedUser = service.addUser { TemporaryUser(it, "Dummy User") }
        val updatedUser = service.updateUser(addedUser.identifier) {
            (it as TemporaryUser).copy(displayName = "New Name")
        }

        assertEquals(addedUser.identifier, updatedUser.identifier)
        assertEquals("Dummy User", addedUser.displayName)
        assertEquals("New Name", updatedUser.displayName)
        assertEquals(updatedUser, service.getUser(updatedUser.identifier))
    }

    @Test
    fun `can update a temporary to a regular user`() {
        val addedUser = service.addUser { TemporaryUser(it, "Dummy User") }
        val updatedUser = service.updateUser(addedUser.identifier) {
            RegularUser(
                it.identifier,
                it.displayName,
                UserCredentials("dummy", "123")
            )
        }

        assertEquals(addedUser.identifier, updatedUser.identifier)
        assertEquals(addedUser.displayName, updatedUser.displayName)
        assertTrue(addedUser is TemporaryUser)
        assertTrue(updatedUser is RegularUser)
        assertEquals(updatedUser, service.getUser(updatedUser.identifier))
        assertEquals(updatedUser, service.findUser("dummy"))
    }

    @Test
    fun `findUser with additional filters`() {
        val addedUser = service.addUser {
            RegularUser(
                it,
                "Dummy User",
                UserCredentials("MyUser", "123")
            )
        }
        assertEquals(addedUser, service.findUser("myuser"))
        assertEquals(addedUser, service.findUser("myuser") {
            it.credentials.password == "123"
        })
        assertNull(service.findUser("myuser") { false })
        assertNull(service.findUser("OtherUser"))
    }

    @Test
    fun `can update score of a user`() {
        val addedUser = service.addUser { TemporaryUser(it, "Dummy User") }
        val updatedScore = service.updateScore(addedUser.identifier) { it + 1 }
        assertEquals(1, updatedScore)
    }

    companion object {
        @JvmStatic
        private fun usernames() =
            Stream.of(
                Arguments.of("username", "username"),
                Arguments.of("username", "USERNAME"),
                Arguments.of("UserNamE", "username"),
            )
    }
}
