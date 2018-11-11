package conduit.handler

import conduit.model.*
import conduit.util.TokenAuth
import io.jsonwebtoken.impl.DefaultClaims
import io.kotlintest.Description
import io.kotlintest.assertSoftly
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class FollowUserHandlerImplTest : StringSpec() {
    lateinit var unit: FollowUserHandlerImpl

    override fun beforeTest(description: Description) {
        unit = FollowUserHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    init {
        "should follow the user" {
            val username = Username("jake")
            val profile = Profile(
                username,
                Bio("I work at statefarm"),
                Image("an image url"),
                true
            )
            val followerEmail = Email("email@site.com")
            every { unit.repository.followUser(username, followerEmail) } returns profile

            val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))

            val result = unit(username, tokenInfo)

            assertSoftly {
                result.bio.shouldBe(profile.bio)
                result.username.shouldBe(profile.username)
                result.image.shouldBe(profile.image)
                result.following.shouldBeTrue()
            }
        }
    }
}