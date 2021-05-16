package ch.chaosconnect.rohan.services

import ch.chaosconnect.rohan.model.User

interface UserService {

    suspend fun signUpAsRegularUser(
        name: String,
        password: String,
        displayName: String
    ): User

    suspend fun signUpAsTemporaryUser(displayName: String): User

    suspend fun signInAsRegularUser(name: String, password: String): User

    suspend fun setPassword(password: String): User

    suspend fun setDisplayName(displayName: String): User

    suspend fun getCurrentUser(): User
}
