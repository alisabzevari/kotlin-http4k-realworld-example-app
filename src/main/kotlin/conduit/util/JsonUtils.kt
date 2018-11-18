package conduit.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import org.http4k.format.ConfigurableJackson
import org.http4k.format.defaultKotlinModuleWithHttp4kSerialisers

val mapper = ObjectMapper()
    .registerModule(defaultKotlinModuleWithHttp4kSerialisers)
    .registerModule(JodaModule())
    .disableDefaultTyping()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
    .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

fun String.toJsonTree() = mapper.readTree(this)

object ConduitJackson : ConfigurableJackson(mapper)