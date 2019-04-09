package conduit.handler

import io.kotlintest.specs.StringSpec

class GetCurrentUserHandlerImplTest : StringSpec() {
    lateinit var unit: GetCurrentUserHandlerImpl

//    override fun beforeTest(testCase: TestCase) {
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