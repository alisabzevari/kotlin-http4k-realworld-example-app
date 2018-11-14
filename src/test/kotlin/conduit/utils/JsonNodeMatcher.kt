package conduit.utils

import com.fasterxml.jackson.databind.JsonNode
import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.should

fun jsonTest(target: JsonNode, expected: JsonNode): Result =
    target.fields().asSequence().map {
        val secondValue = expected[it.key]
        if (secondValue == null) {
            Result(true, "", "")
        } else when {
            it.value.isContainerNode -> jsonTest(it.value, expected[it.key])
            it.value.isValueNode -> Result(
                it.value == secondValue,
                "[${it.key}] should be equal to [$secondValue] but was [${it.value}]",
                "[${it.key}] should not be equal to [$secondValue]"
            )
            it.value.isMissingNode -> Result(
                secondValue.isMissingNode,
                "${it.key} should be missing",
                "${it.key} should not be missing"
            )
            else -> throw Exception("Unexpected Situation in json matching")
        }
    }
        .filterNot { it.passed }
        .fold(Result(true, "Json matching failed", "Json matching failed")) { acc, value ->
            Result(
                value.passed && acc.passed,
                "${acc.failureMessage}\n${value.failureMessage}",
                "${acc.negatedFailureMessage}\n${value.negatedFailureMessage}"
            )
        }

fun containJsonNode(expectedNode: JsonNode) = object: Matcher<JsonNode> {
    override fun test(value: JsonNode): Result = jsonTest(value, expectedNode)
}

fun JsonNode.shouldContainJsonNode(expectedNode: JsonNode) = this should containJsonNode(expectedNode)