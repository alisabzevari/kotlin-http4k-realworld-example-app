package conduit.handler

import conduit.model.ArticleTag
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetTagsHandlerImplTest {
    lateinit var unit: GetTagsHandlerImpl

    @BeforeEach
    fun beforeEach() {
        unit = GetTagsHandlerImpl(
            repository = mockk(relaxed = true)
        )
    }

    @Test
    fun `should return all tags`() {
        val expectedResult = listOf(ArticleTag("1"), ArticleTag("2"))
        every { unit.repository.getTags() } returns expectedResult

        val result = unit()

        assertEquals(expectedResult, result)
    }

}