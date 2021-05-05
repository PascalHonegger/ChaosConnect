package ch.chaosconnect.rohan.services

import ch.chaosconnect.rohan.model.RegularUser
import ch.chaosconnect.rohan.model.User
import ch.chaosconnect.rohan.model.UserScore

interface StorageService {
    fun addUser(processor: (String) -> User): User
    fun updateUser(identifier: String, processor: (User) -> User): User
    fun updateScore(identifier: String, processor: (Long) -> Long): UserScore
    fun getUser(identifier: String): UserScore?
    fun findUser(
        username: String,
        additionalFilter: (RegularUser) -> Boolean = { true }
    ): RegularUser?
}
