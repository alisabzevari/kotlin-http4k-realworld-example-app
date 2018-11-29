package conduit.utils

import com.fasterxml.jackson.databind.JsonNode
import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.should

fun jsonTest(target: JsonNode, expected: JsonNode?, path: String = ""): Result = when {
    expected == null -> sequenceOf(Result(true, "", ""))
    target.isObject -> target.fields().asSequence().map {
        jsonTest(it.value, expected.get(it.key), "${path}.${it.key}")
    }
    target.isArray -> target.elements().asSequence().mapIndexed { index, targetNode ->
        jsonTest(targetNode, expected.get(index), "${path}[$index]")
    }
    target.isValueNode -> sequenceOf(
        Result(
            target == expected,
            "In [$path], [$target] should be equal to [$expected]",
            "In [$path], [$target] should not be equal to [$expected]"
        )
    )
    target.isMissingNode -> sequenceOf(
        Result(
            expected.isMissingNode ?: true,
            "In [$path], $target should be missing",
            "In [$path], $target should not be missing"
        )
    )
    else -> throw Exception("Unexpected Situation in json matching")
}
    .filterNot { it.passed }
    .fold(Result(true, "Json matching failed", "Json matching failed")) { acc, value ->
        Result(
            value.passed && acc.passed,
            "${acc.failureMessage}\n${value.failureMessage}",
            "${acc.negatedFailureMessage}\n${value.negatedFailureMessage}"
        )
    }

fun containJsonNode(expectedNode: JsonNode) = object : Matcher<JsonNode> {
    override fun test(value: JsonNode): Result = jsonTest(value, expectedNode)
}

fun JsonNode.shouldContainJsonNode(expectedNode: JsonNode) = this should containJsonNode(expectedNode)