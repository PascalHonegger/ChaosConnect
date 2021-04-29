package ch.chaosconnect.rohan

import java.util.*
import javax.inject.Singleton

//  TODO: Ensure thread safety
@Singleton
class UserServiceImpl : UserService {

    private val usersByIdentifier = HashMap<String, User>()

    private val regularUsersByName = HashMap<String, RegularUser>()

    override suspend fun signUpAsRegularUser(
        name: String,
        password: String,
        displayName: String
    ): String {
        requireNotBlank(name, "User name")
        requireNotBlank(password, "Password")
        requireNotBlank(displayName, "Display name")
        val identifier = getCurrentIdentifier()
        if (identifier != null) {
            when (usersByIdentifier[identifier]) {
                null ->
                    throw IllegalStateException("Already signed in as different user")
                is RegularUser ->
                    throw IllegalStateException("Already signed in as a regular user")
                is TemporaryUser ->
                    usersByIdentifier.remove(identifier)
            }
        }
        return createRegularUserAndReturnIdentifier(
            identifier,
            name,
            password,
            displayName
        )
    }

    override suspend fun signInAsTemporaryUser(displayName: String): String =
        signIn {
            requireNotBlank(displayName, "Display name")
            createUser(null) {
                TemporaryUser(it, displayName)
            }
        }

    override suspend fun signInAsRegularUser(
        name: String,
        password: String
    ): String =
        signIn {
            val regularUser = regularUsersByName[name]
            if (regularUser != null && regularUser.credentials.password == password) {
                return@signIn regularUser
            }
            throw IllegalAccessException("Sign-in failed")
        }

    override suspend fun setPassword(password: String): String =
        processCurrentUserAndReturnIdentifier {
            when (this) {
                is RegularUser ->
                    credentials.password = password
                else ->
                    throw IllegalAccessException("Cannot set password for temporary user")
            }
        }

    override suspend fun setDisplayName(displayName: String): String =
        processCurrentUserAndReturnIdentifier {
            this.displayName = displayName
        }

    private fun <UserT : User> processUserAndReturnIdentifier(
        user: UserT,
        processor: UserT.() -> Unit
    ): String =
        user
            .apply(processor)
            .identifier

    private fun processCurrentUserAndReturnIdentifier(processor: User.() -> Unit): String =
        getCurrentUser()
            ?.let {
                processUserAndReturnIdentifier(it, processor)
            }
            ?: throw IllegalAccessException("No active user")

    private fun getCurrentUser() =
        getCurrentIdentifier()
            ?.let {
                usersByIdentifier[it]
            }

    private fun createRegularUserAndReturnIdentifier(
        identifier: String?,
        name: String,
        password: String,
        displayName: String
    ): String =
        storeUserByUniqueProperty(regularUsersByName, name, "User name") {
            createUser(identifier) {
                RegularUser(it, displayName, UserCredentials(name, password))
            }
        }
            .identifier

    private fun <UserT : User> createUser(
        identifier: String?,
        factory: (identifier: String) -> UserT
    ): UserT =
        (identifier ?: createUniqueIdentifier())
            .let {
                storeUserByUniqueProperty(
                    usersByIdentifier,
                    it,
                    "User identifier"
                ) {
                    factory(it)
                }
            }

    companion object {

        private fun getCurrentIdentifier() =
            userIdentifierContextKey.get()

        private fun createUniqueIdentifier() =
            UUID.randomUUID().toString()

        private fun <StoredUserT : User, CreatedUserT : StoredUserT, PropertyT> storeUserByUniqueProperty(
            userCache: MutableMap<PropertyT, StoredUserT>,
            property: PropertyT,
            propertyName: String,
            factory: () -> CreatedUserT
        ): CreatedUserT {
            check(!userCache.containsKey(property)) {
                "$propertyName '$property' already in use"
            }
            return factory()
                .apply {
                    userCache[property] = this
                }
        }

        private fun signIn(userProvider: () -> User): String =
            when (getCurrentIdentifier()) {
                null ->
                    userProvider().identifier
                else ->
                    throw IllegalStateException("Already signed in")
            }

        private fun requireNotBlank(value: CharSequence, name: String) =
            require(value.isNotBlank()) {
                "$name may not be blank"
            }
    }
}
