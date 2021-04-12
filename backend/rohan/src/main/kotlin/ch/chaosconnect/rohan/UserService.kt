package ch.chaosconnect.rohan

interface UserService {

    suspend fun getUser(username: String, password: String): String

    suspend fun addUser(username: String, password: String, displayName: String): String

    suspend fun addTemporaryUser(displayName: String): String

    suspend fun updateUser(password: String, displayName: String): String
}
