package ch.chaosconnect.rohan;

import ch.chaosconnect.api.rohan.UserServiceGrpcKt
import ch.chaosconnect.api.user.*
import javax.inject.Singleton

@Singleton
class UserEndpoint(private val service: UserService) :
    UserServiceGrpcKt.UserServiceCoroutineImplBase() {

    override suspend fun getUser(request: GetUserRequest) =
        processRequest {
            service.getUser(
                request.username,
                request.password
            )
        }

    override suspend fun addUser(request: AddUserRequest) =
        processRequest {
            service.addUser(
                request.username,
                request.password,
                request.displayName
            )
        }

    override suspend fun addTemporaryUser(request: AddTemporaryUserRequest) =
        processRequest {
            service.addTemporaryUser(
                request.displayName
            )
        }

    override suspend fun updateUser(request: UpdateUserRequest) =
        processRequest {
            service.updateUser(
                request.password,
                request.displayName
            )
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
