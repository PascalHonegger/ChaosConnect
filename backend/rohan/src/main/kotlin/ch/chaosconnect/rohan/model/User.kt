package ch.chaosconnect.rohan.model

import kotlinx.serialization.Serializable

@Serializable
sealed class User {
    abstract val identifier: String
    abstract val displayName: String
}

@Serializable
data class RegularUser(
    override val identifier: String,
    override val displayName: String,
    val credentials: UserCredentials
) : User()

@Serializable
data class TemporaryUser(
    override val identifier: String,
    override val displayName: String
) : User()

@Serializable
data class UserCredentials(
    val name: String,
    val passwordHash: String
)
