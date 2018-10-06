package conduit.handler

import conduit.model.*
import conduit.util.TokenAuth
import conduit.util.hash
import io.jsonwebtoken.impl.DefaultClaims
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GetCurrentUserHandlerImplTest {
    lateinit var unit: GetCurrentUserHandlerImpl

    @BeforeEach
    fun beforeEach() {
        unit = GetCurrentUserHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    @Test
    fun `should return user info based on tokenInfo`() {
        val password = Password("password")
        val dbUser = User(
            0,
            Email("jake@jake.jake"),
            password.hash(),
            null,
            Username("jake"),
            Bio("I work at statefarm"),
            Image("an image url")
        )
        every { unit.repository.findUserByEmail(any()) } returns dbUser

        val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to "email@site.com")))

        val result = unit(tokenInfo)

        assertEquals(tokenInfo.token, result.token)
        assertEquals(dbUser.bio, result.bio)
        assertEquals(dbUser.email, result.email)
        assertEquals(dbUser.image, result.image)
        assertEquals(dbUser.username, result.username)
    }

    @Test
    fun `should throw exception if user not found`() {
        every { unit.repository.findUserByEmail(any()) } returns null

        val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to "email@site.com")))

        assertThrows<UserNotFoundException> {
            unit(tokenInfo)
        }
    }

}