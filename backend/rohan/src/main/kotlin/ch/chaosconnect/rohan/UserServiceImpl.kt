package ch.chaosconnect.rohan

import java.util.*
import javax.inject.Singleton

//  TODO: Ensure thread safety
@Singleton
class UserServiceImpl : UserService {

    private val usersByIdentifier = HashMap<String, User>()

    private val usersByName = HashMap<String, User>()

    override suspend fun signUpAsRegularUser(
        name: String,
        password: String,
        displayName: String
    ): String {
        checkNameAvailable(name)
        val user = createUser(name, password, displayName)
        usersByName[name] = user
        return user.identifier
    }

    override suspend fun signInAsTemporaryUser(displayName: String): String {
        val user = createUser(null, null, displayName)
        return user.identifier
    }

    override suspend fun signInAsRegularUser(name: String, password: String): String =
        usersByName[name]
            ?.identifier
            ?: throw NoSuchElementException("No user with name '$name' found")

    override suspend fun setPassword(password: String): String {
        //  TODO
        return ""
    }

    override suspend fun setDisplayName(displayName: String): String {
        //  TODO
        return ""
    }

    private fun createUser(
        name: String?,
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
            name,
            password,
            displayName
        )
        usersByIdentifier[identifier] = user
        return user
    }

    private fun checkNameAvailable(name: String) =
        checkUniquePropertyAvailable(usersByName, name, "User name")

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
