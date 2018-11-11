package conduit.handler

import conduit.model.*
import conduit.util.TokenAuth
import io.jsonwebtoken.impl.DefaultClaims
import io.kotlintest.Description
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class UnfollowUserHandlerImplTest : StringSpec() {
    lateinit var unit: UnfollowUserHandlerImpl

    override fun beforeTest(description: Description) {
        unit = UnfollowUserHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    init {
        "should unfollow the user" {
            val username = Username("jake")
            val profile = Profile(
                username,
                Bio("I work at statefarm"),
                Image("an image url"),
                false
            )
            val followerEmail = Email("email@site.com")
            every { unit.repository.unfollowUser(username, followerEmail) } returns profile

            val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))

            val result = unit(username, tokenInfo)

            result.bio.shouldBe(profile.bio)
            result.username.shouldBe(profile.username)
            result.image.shouldBe(profile.image)
            result.following.shouldBeFalse()
        }
    }
}