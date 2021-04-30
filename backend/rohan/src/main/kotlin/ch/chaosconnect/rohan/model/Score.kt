package ch.chaosconnect.rohan.model

import kotlinx.serialization.Serializable

@Serializable
data class UserScore(
    val user: User,
    val score: Long
)
