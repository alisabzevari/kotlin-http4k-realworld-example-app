package conduit.handler

import conduit.model.*
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth
import io.jsonwebtoken.impl.DefaultClaims
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class FollowUserHandlerImplTest : StringSpec() {
    init {
        "follow user should insert following when it is not followed" {
            val repo = mockk<ConduitRepository>(relaxed = true)
            every { repo.getFollowing(any(), any()) } returns false
            every { repo.getUser(any<Username>()) } returns followableUser
            val follow = FollowUserHandlerImpl(TestTxManager(repo))

            val result = follow(username, tokenInfo)

            result.shouldBe(profile)
            verify(exactly = 1) { repo.insertFollowing(any(), followableUserId) }
        }

        "follow user should not change user following if it was already following" {
            val repo = mockk<ConduitRepository>(relaxed = true)
            every { repo.getFollowing(any(), any()) } returns true
            every { repo.getUser(any<Username>()) } returns followableUser
            val follow = FollowUserHandlerImpl(TestTxManager(repo))

            val result = follow(username, tokenInfo)

            result.shouldBe(profile)
            verify(exactly = 0) { repo.insertFollowing(any(), any()) }
        }
    }

    val username = Username("jake")
    val profile = Profile(
        username,
        Bio("I work at statefarm"),
        Image("an image url"),
        true
    )
    val followerEmail = Email("email@site.com")
    val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))
    val followableUserId = 1
    val followableUser = User(followableUserId, Email("email"), Password(""), null, username, profile.bio, profile.image)
}
