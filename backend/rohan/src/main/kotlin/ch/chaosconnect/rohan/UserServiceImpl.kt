package ch.chaosconnect.rohan

import java.util.*
import javax.inject.Singleton

//  TODO: Ensure thread safety
@Singleton
class UserServiceImpl : UserService {

    private val usersByIdentifier = HashMap<String, User>()

    private val usersByUserName = HashMap<String, User>()

    private val usersByDisplayName = HashMap<String, User>()

    override suspend fun getUser(username: String, password: String): String =
        usersByUserName[username]
            ?.identifier
            ?: throw NoSuchElementException("No user with user name '$username' found")

    override suspend fun addUser(
        username: String,
        password: String,
        displayName: String
    ): String {
        checkUserNameAvailable(username)
        checkDisplayNameAvailable(displayName)
        val user = createUser(username, password, displayName)
        usersByUserName[username] = user
        usersByDisplayName[displayName] = user
        return user.identifier
    }

    override suspend fun addTemporaryUser(displayName: String): String {
        checkDisplayNameAvailable(displayName)
        val user = createUser(null, null, displayName)
        usersByDisplayName[displayName] = user
        return user.identifier
    }

    override suspend fun updateUser(
        password: String,
        displayName: String
    ): String {
        val user = usersByDisplayName[displayName]
            ?: throw NoSuchElementException("No user with display name '$displayName' found")
        user.displayName = displayName
        return user.identifier
    }

    private fun createUser(
        username: String?,
        password: String?,
        displayName: String
    ): User {
        val identifier = createUniqueIdentifier()
        //  We are very paranoid
        checkUniquePropertyAvailable(
            usersByIdentifier,
            identifier,
            "User identifier"
        )
        val user = User(
            identifier,
            username,
            password,
            displayName
        )
        usersByIdentifier[identifier] = user
        return user
    }

    private fun checkUserNameAvailable(userName: String) =
        checkUniquePropertyAvailable(usersByUserName, userName, "User name")

    private fun checkDisplayNameAvailable(displayName: String) =
        checkUniquePropertyAvailable(
            usersByDisplayName,
            displayName,
            "Display name"
        )

    companion object {

        private fun createUniqueIdentifier(): String =
            UUID.randomUUID().toString()

        private fun <PropertyT> checkUniquePropertyAvailable(
            userCache: Map<PropertyT, User>,
            property: PropertyT,
            propertyName: String
        ) {
            if (userCache.containsKey(property)) {
                throw IllegalStateException("$propertyName '$property' already in use")
            }
        }
    }
}
