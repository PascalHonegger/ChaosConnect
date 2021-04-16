package ch.chaosconnect.rohan

interface UserService {

    suspend fun signUpAsRegularUser(
        username: String,
        password: String,
        displayName: String
    ): String

    suspend fun signInAsTemporaryUser(displayName: String): String

    suspend fun signInAsRegularUser(username: String, password: String): String

    suspend fun setPassword(password: String): String

    suspend fun setDisplayName(displayName: String): String
}
