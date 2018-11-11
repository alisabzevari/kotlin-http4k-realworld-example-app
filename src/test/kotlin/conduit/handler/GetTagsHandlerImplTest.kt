package conduit.handler

import conduit.model.ArticleTag
import io.kotlintest.Description
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk

class GetTagsHandlerImplTest: StringSpec() {
    lateinit var unit: GetTagsHandlerImpl

    override fun beforeTest(description: Description) {
        unit = GetTagsHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    init {
        "should return all tags" {
            val expectedResult = listOf(ArticleTag("1"), ArticleTag("2"))
            every { unit.repository.getTags() } returns expectedResult

            val result = unit()

            result.shouldBe(expectedResult)
        }
    }
}