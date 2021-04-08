package ch.chaosconnect.joestar

import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties("jwt")
class JwtConfig {
    @NotBlank
    lateinit var secret: String
}