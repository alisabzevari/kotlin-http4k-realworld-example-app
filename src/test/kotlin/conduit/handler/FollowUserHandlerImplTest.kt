package conduit.handler

import io.kotlintest.specs.StringSpec

class FollowUserHandlerImplTest : StringSpec() {
    lateinit var unit: FollowUserHandlerImpl

//    override fun beforeTest(testCase: TestCase) {
//        unit = FollowUserHandlerImpl(
//            repository = mockk(relaxed = true)
//        )
//    }
//
//    init {
//        "should follow the user" {
//            val username = Username("jake")
//            val profile = Profile(
//                username,
//                Bio("I work at statefarm"),
//                Image("an image url"),
//                true
//            )
//            val followerEmail = Email("email@site.com")
//            every { unit.repository.followUser(username, followerEmail) } returns profile
//
//            val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))
//
//            val result = unit(username, tokenInfo)
//
//            assertSoftly {
//                result.bio.shouldBe(profile.bio)
//                result.username.shouldBe(profile.username)
//                result.image.shouldBe(profile.image)
//                result.following.shouldBeTrue()
//            }
//        }
//    }
}