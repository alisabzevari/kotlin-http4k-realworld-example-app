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

class UpdateCurrentUserHandlerImplTest {
    lateinit var unit: UpdateCurrentUserHandlerImpl

    @BeforeEach
    fun beforeEach() {
        unit = UpdateCurrentUserHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    @Test
    fun `should return a user object on successful result`() {
        val dbUser = User(
            0,
            Email("newemail"),
            Password("password").hash(),
            null,
            Username("newusername"),
            Bio("newbio"),
            Image("newimage")
        )
        every { unit.repository.updateUser(any(), any()) } returns dbUser

        val tokenInfo = TokenAuth.TokenInfo(
            Token("token"),
            DefaultClaims(mapOf("email" to "email@site.com"))
        )
        val updateUser = UpdateUser(
            Email("newemail"),
            Username("newusername"),
            Bio("newbio"),
            Image("newimage")
        )

        val result = unit(tokenInfo, updateUser)

        assertEquals(updateUser.bio, result.bio)
        assertEquals(updateUser.email, result.email)
        assertEquals(updateUser.username, result.username)
        assertEquals(updateUser.image, result.image)
    }

}