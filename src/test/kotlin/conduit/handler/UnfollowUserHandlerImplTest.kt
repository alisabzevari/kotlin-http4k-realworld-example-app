package conduit.handler

import conduit.model.*
import conduit.util.TokenAuth
import io.jsonwebtoken.impl.DefaultClaims
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class UnfollowUserHandlerImplTest {
    lateinit var unit: UnfollowUserHandlerImpl

    @BeforeEach
    fun beforeEach() {
        unit = UnfollowUserHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    @Test
    fun `should unfollow the user`() {
        val username = Username("jake")
        val profile = Profile(
            username,
            Bio("I work at statefarm"),
            Image("an image url"),
            false
        )
        val followerEmail = Email("email@site.com")
        every { unit.repository.unfollowUser(username, followerEmail) } returns profile

        val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))

        val result = unit(username, tokenInfo)

        assertEquals(profile.bio, result.bio)
        assertEquals(profile.username, result.username)
        assertEquals(profile.image, result.image)
        assertFalse(result.following)
    }
}