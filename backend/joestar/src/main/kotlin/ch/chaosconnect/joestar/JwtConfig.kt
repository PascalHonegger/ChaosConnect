package ch.chaosconnect.joestar

import io.micronaut.context.annotation.ConfigurationProperties
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
    var clockSkew: Long = 0L
}
