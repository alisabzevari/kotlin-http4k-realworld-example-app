package conduit.handler

import conduit.model.Email
import conduit.model.Token
import conduit.util.TokenAuth
import io.jsonwebtoken.impl.DefaultClaims
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetArticlesFeedHandlerImplTest {
    lateinit var unit: GetArticlesFeedHandlerImpl

    @BeforeEach
    fun beforeEach() {
        unit = GetArticlesFeedHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    @Test
    fun `should return articles feed`() {
        val articlesFeed = MultipleArticles(
            emptyList(),
            10
        )
        every { unit.repository.getArticlesFeed(any(), any(), any()) } returns articlesFeed

        val followerEmail = Email("email@site.com")
        val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))

        val result = unit(tokenInfo, 10, 10)

        assertEquals(articlesFeed.articlesCount, result.articlesCount)
        assertEquals(articlesFeed.articles, result.articles)
    }

}