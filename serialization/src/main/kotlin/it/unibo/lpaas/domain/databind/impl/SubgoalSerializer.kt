package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.domain.Subgoal

class SubgoalSerializer : JsonSerializer<Subgoal>() {
    override fun serialize(value: Subgoal, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeObject(value.value)
    }
}
