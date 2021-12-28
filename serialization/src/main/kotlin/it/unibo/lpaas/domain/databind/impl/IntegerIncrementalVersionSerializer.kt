package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion

class IntegerIncrementalVersionSerializer : JsonSerializer<IntegerIncrementalVersion>() {
    override fun serialize(value: IntegerIncrementalVersion, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value.value)
    }
}
