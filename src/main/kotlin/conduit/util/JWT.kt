package conduit.util

import conduit.model.Email
import conduit.model.Token
import conduit.model.Username
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*
import javax.crypto.spec.SecretKeySpec

class JWT(
    secret: String,
    algorithm: String?,
    private val issuer: String,
    private val expirationMillis: Long
) {

    private val signingKey: SecretKeySpec = SecretKeySpec(
        secret.toByteArray(),
        algorithm ?: SignatureAlgorithm.HS256.jcaName
    )

    fun generate(username: Username, email: Email) = Token(
        Jwts.builder()
            .setSubject(username.value)
            .setIssuer(issuer)
            .setClaims(
                mapOf(
                    "username" to username.value,
                    "email" to email.value
                )
            )
            .setExpiration(Date(System.currentTimeMillis() + expirationMillis))
            .signWith(SignatureAlgorithm.HS256, signingKey)
            .compact()
    )

    fun parse(token: Token): Claims = Jwts
        .parser()
        .setSigningKey(signingKey)
        .parseClaimsJws(token.value)
        .body

}
