package ch.chaosconnect.rohan

interface UserService {

    suspend fun addUser(
        username: String,
        password: String,
        displayName: String
    ): String

    suspend fun addTemporaryUser(displayName: String): String

    suspend fun signInAsRegularUser(username: String, password: String): String

    suspend fun updateUser(password: String, displayName: String): String
}
