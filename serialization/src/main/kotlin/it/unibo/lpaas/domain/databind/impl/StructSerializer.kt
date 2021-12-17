package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.tuprolog.core.Struct
import java.io.IOException

class StructSerializer : JsonSerializer<Struct>() {
    override fun serialize(value: Struct?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (gen == null || value == null) {
            throw IOException("Value or JsonGenerator null.")
        }
        gen.writeString(value.toString())
    }
}
