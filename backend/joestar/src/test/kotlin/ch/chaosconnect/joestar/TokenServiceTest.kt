package ch.chaosconnect.joestar

import io.jsonwebtoken.io.DecodingException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

const val validSecret =
    "kiMu3ODiG8NdA41/6bj5BcsKDUplTh32bmocO3EKbgbOdHGBfST1/dtfuh+hMOTqEGurJgMPI0XCUewlBWEdrw=="
const val invalidSecret = "some text which is definitely not base64"

class TokenServiceTest {
    @Test
    fun `parsed token returns identifier passed when creating token`() {
        val service =
            TokenServiceImpl(JwtConfig().apply {
                secret = validSecret
                issuer = "Me"
                audience = "You"
                validFor = Duration.ofDays(1)
            })
        val token = service.createSignedToken("SomeUsername")
        val decoded = service.parseToken(token)
        assertEquals("SomeUsername", decoded)
    }

    @Test
    fun `invalid secret throws error`() {
        assertThrows<DecodingException> {
            TokenServiceImpl(JwtConfig().apply {
                secret = invalidSecret
                issuer = "Me"
                audience = "You"
                validFor = Duration.ofDays(1)
            })
        }
    }
}
