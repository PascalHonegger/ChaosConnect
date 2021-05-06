package ch.chaosconnect.rohan.services

import ch.chaosconnect.rohan.meta.userIdentifierContextKey
import ch.chaosconnect.rohan.model.RegularUser
import ch.chaosconnect.rohan.model.TemporaryUser
import ch.chaosconnect.rohan.model.User
import ch.chaosconnect.rohan.model.UserCredentials
import de.mkammerer.argon2.Argon2Factory
import javax.inject.Singleton
import javax.security.auth.login.AccountException
import javax.security.auth.login.FailedLoginException

private fun requireNotBlank(value: CharSequence, name: String) =
    require(value.isNotBlank()) {
        "$name may not be blank"
    }

private fun getCurrentIdentifier() = userIdentifierContextKey.get()

private val argon2 = Argon2Factory.create()

@Singleton
class UserServiceImpl(private val storageService: StorageService) :
    UserService {
    override suspend fun signUpAsRegularUser(
        name: String,
        password: String,
        displayName: String
    ): String = signUp {
        requireNotBlank(name, "User name")
        requireNotBlank(password, "Password")
        requireNotBlank(displayName, "Display name")

        val credentials =
            UserCredentials(name = name, passwordHash = hashPassword(password))

        when (val currentIdentifier = getCurrentIdentifier()) {
            null -> storageService.addUser {
                RegularUser(
                    identifier = it,
                    displayName = displayName,
                    credentials = credentials
                )
            }
            else -> storageService.updateUser(currentIdentifier) {
                when (it) {
                    is RegularUser -> error("Already signed in as a regular user")
                    is TemporaryUser -> RegularUser(
                        identifier = it.identifier,
                        displayName = it.displayName,
                        credentials = credentials
                    )
                }
            }
        }
    }

    override suspend fun signInAsTemporaryUser(displayName: String): String =
        signIn {
            requireNotBlank(displayName, "Display name")
            storageService.addUser { TemporaryUser(it, displayName) }
        }

    override suspend fun signInAsRegularUser(
        name: String,
        password: String
    ): String = signIn {
        storageService.findUser(name) {
            verifyPassword(
                hash = it.credentials.passwordHash,
                password = password
            )
        }
            ?: throw FailedLoginException("Sign-in failed")
    }

    override suspend fun setPassword(password: String): String = update {
        val currentIdentifier = getCurrentIdentifier()
        checkNotNull(currentIdentifier) { "No active user" }
        storageService.updateUser(currentIdentifier) {
            when (it) {
                is RegularUser -> it.copy(
                    credentials = it.credentials.copy(
                        passwordHash = hashPassword(password)
                    )
                )
                is TemporaryUser -> throw AccountException("Cannot set password for temporary user")
            }
        }
    }

    override suspend fun setDisplayName(displayName: String): String = update {
        val currentIdentifier = getCurrentIdentifier()
        checkNotNull(currentIdentifier) { "No active user" }
        storageService.updateUser(currentIdentifier) {
            when (it) {
                is RegularUser -> it.copy(displayName = displayName)
                is TemporaryUser -> it.copy(displayName = displayName)
            }
        }
    }

    private fun signIn(userProvider: () -> User): String =
        when (getCurrentIdentifier()) {
            null -> userProvider().identifier
            else -> error("Already signed in")
        }

    private fun signUp(userProvider: () -> User): String =
        userProvider().identifier

    private fun update(userProvider: () -> User): String =
        userProvider().identifier

    private fun hashPassword(password: String): String =
        argon2.hash(10, 65536, 1, password.toCharArray())

    private fun verifyPassword(hash: String, password: String): Boolean =
        argon2.verify(hash, password.toCharArray())
}
