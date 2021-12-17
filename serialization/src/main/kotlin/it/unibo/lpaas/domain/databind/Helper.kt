package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper

fun <A, B> JsonDeserializer<A>.derive(f: (A) -> B): JsonDeserializer<B> =
    object : JsonDeserializer<B>() {
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): B =
            f(this@derive.deserialize(p, ctxt))
    }

fun configureMappers(mappers: List<ObjectMapper>, fn: ObjectMapper.() -> Unit) {
    mappers.forEach(fn)
}

fun configureMappers(vararg mappers: ObjectMapper, fn: ObjectMapper.() -> Unit) {
    configureMappers(mappers.toList(), fn)
}
