package conduit.util

import conduit.model.Token
import conduit.model.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*
import javax.crypto.spec.SecretKeySpec

private val signingKey = SecretKeySpec("Top Secret".toByteArray(), SignatureAlgorithm.HS256.jcaName)

fun generateToken(user: User) = Token(
    Jwts.builder()
        .setSubject(user.username.value)
        .setIssuer("thinkster.io")
        .setClaims(
            mapOf(
                "id" to user.id,
                "email" to user.email.value
            )
        )
        .setExpiration(Date(System.currentTimeMillis() + 36_000_000))
        .signWith(SignatureAlgorithm.HS256, signingKey)
        .compact()
)

fun Token.parse() = Jwts
    .parser()
    .setSigningKey(signingKey)
    .parseClaimsJws(value)
    .body