package conduit.util

import conduit.model.Email
import conduit.model.Token
import conduit.model.Username
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*
import javax.crypto.spec.SecretKeySpec

fun generateToken(signingKey: SecretKeySpec, issuer: String, expirationMillis: Long, username: Username, email: Email) = Token(
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

fun Token.parse(signingKey: SecretKeySpec): Claims = Jwts
    .parser()
    .setSigningKey(signingKey)
    .parseClaimsJws(value)
    .body
