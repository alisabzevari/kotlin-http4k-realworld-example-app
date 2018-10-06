package conduit.handler

import conduit.model.*
import conduit.util.TokenAuth
import conduit.util.hash
import io.jsonwebtoken.impl.DefaultClaims
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FollowUserHandlerImplTest {
    lateinit var unit: FollowUserHandlerImpl

    @BeforeEach
    fun beforeEach() {
        unit = FollowUserHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    @Test
    fun `should follow the user`() {
        val username = Username("jake")
        val dbUser = Profile(
            username,
            Bio("I work at statefarm"),
            Image("an image url"),
            true
        )
        val followerEmail = Email("email@site.com")
        every { unit.repository.followUser(username, followerEmail) } returns dbUser

        val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))

        val result = unit(username, tokenInfo)

        assertEquals(dbUser.bio, result.bio)
        assertEquals(dbUser.username, result.username)
        assertEquals(dbUser.image, result.image)
        assertEquals(dbUser.following, result.following)
    }

}