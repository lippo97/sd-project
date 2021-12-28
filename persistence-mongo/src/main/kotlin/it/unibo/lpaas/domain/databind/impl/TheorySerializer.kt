package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.tuprolog.serialize.TheoryObjectifier
import it.unibo.tuprolog.theory.Theory

class TheorySerializer : JsonSerializer<Theory>() {
    override fun serialize(value: Theory, gen: JsonGenerator, serializers: SerializerProvider) {
        val objectified = TheoryObjectifier.default.objectify(value)
        gen.writeObject(objectified)
    }
}
