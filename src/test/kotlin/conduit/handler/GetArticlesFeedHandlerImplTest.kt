package conduit.handler

import io.kotlintest.specs.StringSpec

class GetArticlesFeedHandlerImplTest : StringSpec() {
    lateinit var unit: GetArticlesFeedHandlerImpl

//    override fun beforeTest(testCase: TestCase) {
//        unit = GetArticlesFeedHandlerImpl(
//            repository = mockk(relaxed = true)
//        )
//    }
//
//    init {
//        "should return articles feed" {
//            val articlesFeed = MultipleArticles(
//                emptyList(),
//                10
//            )
//            every { unit.repository.getArticlesFeed(any(), any(), any()) } returns articlesFeed
//
//            val followerEmail = Email("email@site.com")
//            val tokenInfo = TokenAuth.TokenInfo(Token("token"), DefaultClaims(mapOf("email" to followerEmail.value)))
//
//            val result = unit(tokenInfo, 10, 10)
//
//            result.articlesCount.shouldBe(articlesFeed.articlesCount)
//            result.articles.shouldBe(articlesFeed.articles)
//        }
//    }
}