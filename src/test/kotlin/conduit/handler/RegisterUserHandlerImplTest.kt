package conduit.handler

import conduit.model.Email
import conduit.model.Password
import conduit.model.Username
import conduit.repository.UserAlreadyExistsException
import conduit.util.parse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterUserHandlerImplTest {
    lateinit var unit: RegisterUserHandlerImpl

    @BeforeEach
    fun beforeEach() {
        unit = RegisterUserHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    @Test
    fun `should return a user object on successful result`() {
        val newUser = NewUserDto(
            Username("name"),
            Password("password"),
            Email("email@site.com")
        )

        val result = unit(newUser)

        assertEquals(newUser.email, result.email)
        assertEquals(newUser.username, result.username)
        val parsedToken = result.token.parse()
        assertEquals(newUser.username.value, parsedToken["username"])
        assertEquals(newUser.email.value, parsedToken["email"])
    }

    @Test
    fun `should throw exception if the user already exists`() {
        every { unit.repository.insertUser(any()) } throws UserAlreadyExistsException()
        val newUser = NewUserDto(
            Username("name"),
            Password("password"),
            Email("email@site.com")
        )

        val exception = assertThrows<UserAlreadyExistsException> {
            unit(newUser)
        }

        assertTrue(exception.message!!.contains("The specified user already exists."))
    }

}