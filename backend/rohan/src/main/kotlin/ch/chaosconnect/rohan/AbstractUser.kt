package ch.chaosconnect.rohan

internal abstract class AbstractUser(
    override val identifier: String,
    override var displayName: String
) : User
