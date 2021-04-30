package ch.chaosconnect.rohan

import ch.chaosconnect.api.rohan.UserServiceGrpcKt
import ch.chaosconnect.api.user.*
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
            service.signInAsTemporaryUser(
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
                else ->
                    throw IllegalArgumentException("Unknown changed property")
            }
        }

    companion object {
        private suspend fun processRequest(
            processor: suspend () -> String
        ): UserAuthResponse {
            val builder = UserAuthResponse.newBuilder()
            try {
                builder.identifier = processor()
            } catch (throwable: Throwable) {
                builder.failureReason = throwable.message
            }
            return builder.build()
        }
    }
}
