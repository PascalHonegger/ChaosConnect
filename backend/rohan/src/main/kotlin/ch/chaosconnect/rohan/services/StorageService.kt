package ch.chaosconnect.rohan.services

import ch.chaosconnect.rohan.model.RegularUser
import ch.chaosconnect.rohan.model.User

interface StorageService {
    fun addUser(processor: (String) -> User): User
    fun updateUser(identifier: String, processor: (User) -> User): User
    fun updateScore(identifier: String, processor: (Long) -> Long): Long
    fun getUser(identifier: String): User?
    fun findUser(
        username: String,
        additionalFilter: (RegularUser) -> Boolean = { true }
    ): RegularUser?
}
