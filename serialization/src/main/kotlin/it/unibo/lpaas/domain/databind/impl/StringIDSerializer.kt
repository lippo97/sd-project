package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.domain.impl.StringId

class StringIDSerializer : JsonSerializer<StringId>() {
    override fun serialize(value: StringId, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString(value.value)
    }
}
