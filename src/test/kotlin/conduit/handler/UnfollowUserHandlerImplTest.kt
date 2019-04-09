package conduit.handler

import io.kotlintest.specs.StringSpec

class UnfollowUserHandlerImplTest : StringSpec() {
    lateinit var unit: UnfollowUserHandlerImpl

//    override fun beforeTest(testCase: TestCase) {
//        unit = UnfollowUserHandlerImpl(
//            repository = mockk(relaxed = true)
//        )
//    }
//
//    init {
//        "should unfollow the user" {
//            val username = Username("jake")
//            val profile = Profile(
//                username,
//                Bio("I work at statefarm"),
//                Image("an image url"),
//                false
//            )
//            val followerEmail = Email("email@site.com")
//            every { unit.repository.unfollowUser(username, followerEmail) } returns profile
//
//            val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))
//
//            val result = unit(username, tokenInfo)
//
//            result.bio.shouldBe(profile.bio)
//            result.username.shouldBe(profile.username)
//            result.image.shouldBe(profile.image)
//            result.following.shouldBeFalse()
//        }
//    }
}