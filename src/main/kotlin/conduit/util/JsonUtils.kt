package conduit.util

import com.fasterxml.jackson.databind.ObjectMapper

val mapper = ObjectMapper()

fun Any.stringifyAsJson() = mapper.writeValueAsString(this)

fun String.toJsonTree() = mapper.readTree(this)