package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.domain.Variable

class VariableSerializer : JsonSerializer<Variable>() {
    override fun serialize(variable: Variable, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(variable.value)
    }
}
