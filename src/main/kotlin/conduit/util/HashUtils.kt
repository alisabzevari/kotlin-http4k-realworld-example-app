package conduit.util

import conduit.model.Password
import java.security.MessageDigest

fun Password.hash(): Password {
    val digest = MessageDigest.getInstance("SHA-256")
    return Password(digest.digest(this.value.toByteArray()).toString(Charsets.UTF_8))
}