package ch.chaosconnect.rohan

interface UserService {

    fun getUser(username: String, password: String): String

    fun addUser(username: String, password: String, displayName: String): String

    fun addTemporaryUser(displayName: String): String

    fun updateUser(password: String, displayName: String): String
}
