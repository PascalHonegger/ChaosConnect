package ch.chaosconnect.rohan

import javax.inject.Singleton

@Singleton
class UserServiceImpl : UserService {

    private val usersByUserName = HashMap<String, User>()

    private val usersByDisplayName = HashMap<String, User>()

    override fun getUser(username: String, password: String): String =
        usersByUserName[username]
            ?.displayName
            ?: throw NoSuchElementException("No user with user name '$username' found")

    override fun addUser(
        username: String,
        password: String,
        displayName: String
    ): String {
        checkUserNameAvailable(username)
        checkDisplayNameAvailable(displayName)
        val user = User(
            username,
            password,
            displayName
        )
        usersByUserName[username] = user
        usersByDisplayName[displayName] = user
        return displayName;
    }

    override fun addTemporaryUser(displayName: String): String {
        checkDisplayNameAvailable(displayName)
        usersByDisplayName[displayName] = User(
            null,
            null,
            displayName
        )
        return displayName;
    }

    override fun updateUser(password: String, displayName: String): String {
        val user = usersByDisplayName[displayName]
            ?: throw NoSuchElementException("No user with display name '$displayName' found")
        user.displayName = displayName
        return displayName
    }

    private fun checkUserNameAvailable(userName: String) =
        checkNameAvailable(usersByUserName, userName, "User")

    private fun checkDisplayNameAvailable(displayName: String) =
        checkNameAvailable(usersByDisplayName, displayName, "Display")

    companion object {

        private fun checkNameAvailable(
            userCache: MutableMap<String, User>,
            name: String,
            nameKind: String
        ) {
            if (userCache.containsKey(name)) {
                throw IllegalStateException("$nameKind name '$name' already in use")
            }
        }
    }
}
