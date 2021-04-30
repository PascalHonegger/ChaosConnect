package ch.chaosconnect.joestar

import ch.chaosconnect.api.authentication.*
import ch.chaosconnect.api.common.Empty
import ch.chaosconnect.api.joestar.WebLoginServiceGrpcKt
import ch.chaosconnect.api.user.UserAuthResponse
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class WebLoginEndpoint(
    private val tokenService: TokenService,
    private val rohanService: RohanService
) : WebLoginServiceGrpcKt.WebLoginServiceCoroutineImplBase() {

    private fun UserAuthResponse?.asTokenResponseOrError(): TokenResponse =
        if (this != null && this.hasIdentifier()) {
            TokenResponse.newBuilder()
                .setJwtToken(tokenService.createSignedToken(this.identifier))
                .build()
        } else {
            throw Status.UNAUTHENTICATED.withDescription("Authentication with backend failed")
                .asException()
        }

    override suspend fun login(request: LoginRequest) =
        rohanService.login(request.username, request.password)
            .asTokenResponseOrError()

    override suspend fun playWithoutAccount(request: PlayWithoutAccountRequest) =
        rohanService.playWithoutAccount(request.displayName)
            .asTokenResponseOrError()

    override suspend fun register(request: RegisterRequest) =
        rohanService.register(
            request.displayName,
            request.username,
            request.password
        ).asTokenResponseOrError()

    override suspend fun renewToken(request: Empty) =
        when (val existingToken = currentUserIdentifier.get()) {
            null -> null
            // We assume that the user still exists in the backend
            // If not, game service calls made with an invalid user will fail
            else -> UserAuthResponse.newBuilder().setIdentifier(existingToken)
                .build()
        }.asTokenResponseOrError()


    override suspend fun updateMetaState(request: UpdateMetaStateRequest) =
        when {
            currentUserIdentifier.get() == null -> null
            request.hasDisplayName() -> rohanService.setDisplayName(request.displayName)
            request.hasPassword() -> rohanService.setPassword(request.password)
            else -> null
        }.asTokenResponseOrError()
}
