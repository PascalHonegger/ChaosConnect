package ch.chaosconnect.rohan

internal class RegularUser(
    identifier: String,
    displayName: String,
    val credentials: UserCredentials
) : AbstractUser(identifier, displayName)
