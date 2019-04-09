package conduit.handler

import conduit.model.*
import conduit.util.hash
import conduit.util.parse
import io.kotlintest.Description
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class LoginHandlerImplTest : StringSpec() {
    lateinit var unit: LoginHandlerImpl

//    override fun beforeTest(description: Description) {
//        unit = LoginHandlerImpl(
//            repository = mockk(relaxed = true)
//        )
//    }
//
//    init {
//        "should return a fresh token along with user in response" {
//            val password = Password("password")
//            val dbUser = User(
//                0,
//                Email("jake@jake.jake"),
//                password.hash(),
//                null,
//                Username("jake"),
//                Bio("I work at statefarm"),
//                Image("an image url")
//            )
//            every { unit.repository.findUserByEmail(any()) } returns dbUser
//            val loginInfo = LoginUserDto(
//                Email("jake@jake.jake"),
//                Password("password")
//            )
//
//            val result = unit(loginInfo)
//
//            val parsedToken = result.token.parse()
//
//            result.bio.shouldBe(dbUser.bio)
//            result.email.shouldBe(dbUser.email)
//            result.image.shouldBe(dbUser.image)
//            result.username.shouldBe(dbUser.username)
//            parsedToken["username"].shouldBe(dbUser.username.value)
//            parsedToken["email"].shouldBe(dbUser.email.value)
//        }
//
//        "should throw exception if password does not match" {
//            val password = Password("password")
//            val dbUser = User(
//                0,
//                Email("jake@jake.jake"),
//                password.hash(),
//                null,
//                Username("jake"),
//                Bio("I work at statefarm"),
//                Image("an image url")
//            )
//            every { unit.repository.findUserByEmail(any()) } returns dbUser
//            val loginInfo = LoginUserDto(
//                Email("jake@jake.jake"),
//                Password("wrong password")
//            )
//
//            shouldThrow<InvalidUserPassException> {
//                unit(loginInfo)
//            }
//        }
//    }
}

