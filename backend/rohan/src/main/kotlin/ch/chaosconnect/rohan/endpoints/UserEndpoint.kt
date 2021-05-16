package ch.chaosconnect.rohan.endpoints

import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.rohan.UserServiceGrpcKt
import ch.chaosconnect.api.user.*
import ch.chaosconnect.rohan.model.RegularUser
import ch.chaosconnect.rohan.model.TemporaryUser
import ch.chaosconnect.rohan.model.User
import ch.chaosconnect.rohan.services.UserService
import javax.inject.Singleton

@Singleton
class UserEndpoint(private val service: UserService) :
    UserServiceGrpcKt.UserServiceCoroutineImplBase() {

    override suspend fun getUser(request: GetUserRequest) =
        processRequest {
            service.signInAsRegularUser(
                request.username,
                request.password
            )
        }

    override suspend fun addUser(request: AddUserRequest) =
        processRequest {
            service.signUpAsRegularUser(
                request.username,
                request.password,
                request.displayName
            )
        }

    override suspend fun addTemporaryUser(request: AddTemporaryUserRequest) =
        processRequest {
            service.signUpAsTemporaryUser(
                request.displayName
            )
        }

    override suspend fun updateUser(request: UpdateUserRequest) =
        processRequest {
            when {
                request.hasPassword() ->
                    service.setPassword(request.password)
                request.hasDisplayName() ->
                    service.setDisplayName(request.displayName)
                else -> error("Unknown changed property")
            }
        }

    override suspend fun renewToken(request: Empty) =
        processRequest {
            service.getCurrentUser()
        }

    companion object {
        private suspend fun processRequest(
            processor: suspend () -> User
        ): UserAuthResponse {
            val builder = UserAuthResponse.newBuilder()
            try {
                val user = processor()
                builder.token = UserTokenContent.newBuilder().apply {
                    identifier = user.identifier
                    playerType = when (user) {
                        is RegularUser -> PlayerType.REGULAR
                        is TemporaryUser -> PlayerType.TEMPORARY
                    }
                }.build()
            } catch (throwable: Throwable) {
                builder.failureReason = throwable.message
            }
            return builder.build()
        }
    }
}
