package conduit.handler

import conduit.model.*
import conduit.util.TokenAuth
import conduit.util.hash
import io.jsonwebtoken.impl.DefaultClaims
import io.kotlintest.Description
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class UpdateCurrentUserHandlerImplTest: StringSpec() {
    lateinit var unit: UpdateCurrentUserHandlerImpl

//    override fun beforeTest(description: Description) {
//        unit = UpdateCurrentUserHandlerImpl(
//            repository = mockk(relaxed = true)
//        )
//    }
//
//    init {
//        "should return a user object on successful result" {
//            val dbUser = User(
//                0,
//                Email("newemail"),
//                Password("password").hash(),
//                null,
//                Username("newusername"),
//                Bio("newbio"),
//                Image("newimage")
//            )
//            every { unit.repository.updateUser(any(), any()) } returns dbUser
//
//            val tokenInfo = TokenAuth.TokenInfo(
//                Token("token"),
//                DefaultClaims(mapOf("email" to "email@site.com"))
//            )
//            val updateUser = UpdateUser(
//                Email("newemail"),
//                Username("newusername"),
//                Bio("newbio"),
//                Image("newimage")
//            )
//
//            val result = unit(tokenInfo, updateUser)
//
//            result.bio.shouldBe(updateUser.bio)
//            result.email.shouldBe(updateUser.email)
//            result.username.shouldBe(updateUser.username)
//            result.image.shouldBe(updateUser.image)
//        }
//    }
}