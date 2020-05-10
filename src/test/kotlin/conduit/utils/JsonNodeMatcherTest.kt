package conduit.utils

import conduit.util.toJsonTree
import io.kotest.core.spec.style.StringSpec
import org.intellij.lang.annotations.Language

class JsonNodeMatcherTest: StringSpec() {
    init {
        "should be able to match a json holding a string" {
            @Language("JSON")
            val obj1 = """
              {
                "items": ["1", "2"],
                "item": {
                  "f1": true,
                  "f2": "f2",
                  "f3": 1,
                  "f4": [1, 2],
                  "f5": 1
                }
              }
            """.trimIndent().toJsonTree()

            @Language("JSON")
            val obj2 = """
              {
                "items": ["1", "2"],
                "item": {
                  "f1": true,
                  "f2": "f2",
                  "f3": 1,
                  "f4": [1, 2]
                }
              }
            """.trimIndent().toJsonTree()

            obj1.shouldContainJsonNode(obj2)
        }
    }
}
