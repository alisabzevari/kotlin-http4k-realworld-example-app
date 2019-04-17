package conduit.util

import conduit.model.Email
import conduit.model.Token
import conduit.model.Username
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*
import javax.crypto.spec.SecretKeySpec

private val signingKey = SecretKeySpec("Top Secret".toByteArray(), SignatureAlgorithm.HS256.jcaName)

fun generateToken(username: Username, email: Email) = Token(
    Jwts.builder()
        .setSubject(username.value)
        .setIssuer("thinkster.io")
        .setClaims(
            mapOf(
                "username" to username.value,
                "email" to email.value
            )
        )
        .setExpiration(Date(System.currentTimeMillis() + 36_000_000))
        .signWith(SignatureAlgorithm.HS256, signingKey)
        .compact()
)

fun Token.parse(): Claims = Jwts
    .parser()
    .setSigningKey(signingKey)
    .parseClaimsJws(value)
    .body