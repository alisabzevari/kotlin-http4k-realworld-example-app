package conduit.handler

import conduit.model.*
import conduit.util.hash
import conduit.util.parse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class LoginHandlerImplTest {
    lateinit var unit: LoginHandlerImpl

    @BeforeEach
    fun beforeEach() {
        unit = LoginHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    @Test
    fun `should return a fresh token along with user in response`() {
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
        val loginInfo = LoginUser(
            Email("jake@jake.jake"),
            Password("password")
        )

        val result = unit(loginInfo)

        assertEquals(dbUser.bio, result.bio)
        assertEquals(dbUser.email, result.email)
        assertEquals(dbUser.image, result.image)
        assertEquals(dbUser.username, result.username)
        val parsedToken = result.token.parse()
        assertEquals(dbUser.username.value, parsedToken["username"])
        assertEquals(dbUser.email.value, parsedToken["email"])
    }

    @Test
    fun `should throw exception if password does not match`() {
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
        val loginInfo = LoginUser(
            Email("jake@jake.jake"),
            Password("wrong password")
        )

        assertThrows<InvalidUserPassException> {
            unit(loginInfo)
        }
    }
}

