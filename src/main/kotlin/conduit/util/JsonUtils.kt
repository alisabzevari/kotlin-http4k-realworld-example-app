package conduit.util

import com.fasterxml.jackson.databind.ObjectMapper

val mapper = ObjectMapper()

fun String.toJsonTree() = mapper.readTree(this)