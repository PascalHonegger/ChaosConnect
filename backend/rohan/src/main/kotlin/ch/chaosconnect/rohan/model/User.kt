package ch.chaosconnect.rohan.model

interface User {
    val identifier: String
    var displayName: String
}

abstract class AbstractUser(
    override val identifier: String,
    override var displayName: String
) : User

class RegularUser(
    identifier: String,
    displayName: String,
    val credentials: UserCredentials
) : AbstractUser(identifier, displayName)

class TemporaryUser(identifier: String, displayName: String) :
    AbstractUser(identifier, displayName)

class UserCredentials(
    val name: String,
    var password: String
)
