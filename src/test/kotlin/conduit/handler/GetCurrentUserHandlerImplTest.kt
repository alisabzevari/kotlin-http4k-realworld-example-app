package conduit.handler

import conduit.model.*
import conduit.repository.UserNotFoundException
import conduit.util.TokenAuth
import conduit.util.hash
import io.jsonwebtoken.impl.DefaultClaims
import io.kotlintest.Description
import io.kotlintest.assertSoftly
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class GetCurrentUserHandlerImplTest : StringSpec() {
    lateinit var unit: GetCurrentUserHandlerImpl

//    override fun beforeTest(description: Description) {
//        unit = GetCurrentUserHandlerImpl(
//            repository = mockk(relaxed = true)
//        )
//    }
//
//    init {
//        "should return user info based on tokenInfo" {
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
//
//            val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to "email@site.com")))
//
//            val result = unit(tokenInfo)
//
//            assertSoftly {
//                result.token.shouldBe(tokenInfo.token)
//                result.bio.shouldBe(dbUser.bio)
//                result.email.shouldBe(dbUser.email)
//                result.image.shouldBe(dbUser.image)
//                result.username.shouldBe(dbUser.username)
//            }
//        }
//
//        "should throw exception if user not found" {
//            every { unit.repository.findUserByEmail(any()) } returns null
//
//            val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to "email@site.com")))
//
//            shouldThrow<UserNotFoundException> {
//                unit(tokenInfo)
//            }
//        }
//    }
}