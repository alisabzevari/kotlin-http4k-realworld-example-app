package conduit.handler

import io.kotlintest.specs.StringSpec

class GetTagsHandlerImplTest: StringSpec() {
    lateinit var unit: GetTagsHandlerImpl

//    override fun beforeTest(testCase: TestCase) {
//        unit = GetTagsHandlerImpl(
//            repository = mockk(relaxed = true)
//        )
//    }
//
//    init {
//        "should return all tags" {
//            val expectedResult = listOf(ArticleTag("1"), ArticleTag("2"))
//            every { unit.repository.getTags() } returns expectedResult
//
//            val result = unit()
//
//            result.shouldBe(expectedResult)
//        }
//    }
}