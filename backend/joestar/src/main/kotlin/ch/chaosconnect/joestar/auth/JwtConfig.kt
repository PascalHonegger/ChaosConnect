package ch.chaosconnect.joestar.auth

import io.micronaut.context.annotation.ConfigurationProperties
import java.time.Duration
import javax.validation.constraints.NotBlank

@ConfigurationProperties("jwt")
class JwtConfig {
    @NotBlank
    lateinit var secret: String

    @NotBlank
    lateinit var issuer: String

    @NotBlank
    lateinit var audience: String

    @NotBlank
    lateinit var validFor: Duration

    var clockSkew: Duration = Duration.ZERO
}
