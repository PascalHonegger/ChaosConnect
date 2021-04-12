package ch.chaosconnect.joestar

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey
import javax.inject.Singleton


private val logger =
    LoggerFactory.getLogger(TokenServiceImpl::class.java)

interface TokenService {
    /**
     * Encodes the given user identity into a signed json web token
     * @return the signed json web token
     */
    fun createSignedToken(identifier: String): String

    /**
     * @param jwtToken signed json web token to verify and parse
     * @return the parsed user identifier if a valid token was provided, null otherwise
     */
    fun parseToken(jwtToken: String): String?
}

@Singleton
class TokenServiceImpl(private val jwtConfig: JwtConfig) : TokenService {

    private var key: SecretKey
    private var jwtParser: JwtParser

    /**
     * Encodes the given user identity into a signed json web token
     * @return the signed json web token
     */
    override fun createSignedToken(identifier: String): String {
        val now = Instant.now()
        return Jwts.builder()
            .signWith(key)
            .setIssuer(jwtConfig.issuer)
            .setSubject(identifier)
            .setAudience(jwtConfig.audience)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(jwtConfig.validFor)))
            .compact()
    }

    /**
     * @param jwtToken signed json web token to verify and parse
     * @return the parsed user identifier if a valid token was provided, null otherwise
     */
    override fun parseToken(jwtToken: String): String? {
        try {
            return jwtParser.parseClaimsJws(jwtToken).body.subject
        } catch (mce: MissingClaimException) {
            logger.warn(
                "JWT did not contain expected claims, rejecting token",
                mce
            )
        } catch (ice: IncorrectClaimException) {
            logger.warn("JWT contained unexpected claims, rejecting token", ice)
        } catch (je: JwtException) {
            logger.warn("Unexpected JWT validation error, rejecting token", je)
        }
        return null
    }

    init {
        try {
            val privateKeyBytes = Decoders.BASE64.decode(jwtConfig.secret)
            key = Keys.hmacShaKeyFor(privateKeyBytes)
        } catch (e: Exception) {
            logger.error(
                "Expected a base64 string containing a key, failed to parse",
                e
            )
            throw e
        }
        logger.info("Parsed private key with algorithm ${key.algorithm}")
        jwtParser = Jwts.parserBuilder()
            .setSigningKey(key)
            .requireIssuer(jwtConfig.issuer)
            .requireAudience(jwtConfig.audience)
            .setAllowedClockSkewSeconds(jwtConfig.clockSkew.seconds)
            .build()
    }
}
