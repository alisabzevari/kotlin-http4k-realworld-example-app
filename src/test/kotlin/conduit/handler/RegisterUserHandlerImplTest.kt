package conduit.handler

import conduit.model.Email
import conduit.model.Password
import conduit.model.Username
import conduit.repository.UserAlreadyExistsException
import conduit.util.parse
import io.kotlintest.Description
import io.kotlintest.TestCase
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class RegisterUserHandlerImplTest : StringSpec() {
    lateinit var unit: RegisterUserHandlerImpl

    override fun beforeTest(testCase: TestCase) {
        unit = RegisterUserHandlerImpl(
            database = mockk(relaxed = true)
        )
    }

    init {
        "should return a user object on successful result" {
            val newUser = NewUserDto(
                Username("name"),
                Password("password"),
                Email("email@site.com")
            )

            val result = unit(newUser)

            val parsedToken = result.token.parse()

            result.email.shouldBe(newUser.email)
            result.username.shouldBe(newUser.username)
            parsedToken["username"].shouldBe(newUser.username.value)
            parsedToken["email"].shouldBe(newUser.email.value)
        }

        // TODO: fix this
//        "should throw exception if the user already exists" {
//            every { unit.database.insertUser(any()) } throws UserAlreadyExistsException()
//            val newUser = NewUserDto(
//                Username("name"),
//                Password("password"),
//                Email("email@site.com")
//            )
//
//            val exception = shouldThrow<UserAlreadyExistsException> {
//                unit(newUser)
//            }
//
//            exception.message!!.shouldContain("The specified user already exists.")
//        }
    }
}