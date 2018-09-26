package conduit.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.http4k.format.Json

val mapper = ObjectMapper()

fun Any.stringifyAsJson() = mapper.writeValueAsString(this)

fun String.toJsonTree() = mapper.readTree(this)