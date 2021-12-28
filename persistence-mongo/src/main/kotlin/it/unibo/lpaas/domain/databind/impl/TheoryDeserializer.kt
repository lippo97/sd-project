package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import it.unibo.tuprolog.serialize.TheoryDeobjectifier
import it.unibo.tuprolog.theory.Theory

class TheoryDeserializer : JsonDeserializer<Theory>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Theory =
        TheoryDeobjectifier.default.deobjectify(p.readValueAs(Any::class.java))
}
