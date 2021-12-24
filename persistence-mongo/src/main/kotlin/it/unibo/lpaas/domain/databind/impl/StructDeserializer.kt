package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.serialize.TermDeobjectifier
import java.io.IOException

class StructDeserializer : JsonDeserializer<Struct>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Struct {
        val term = TermDeobjectifier.default.deobjectify(p.readValueAs(Any::class.java))
        return if (term.isStruct) term as Struct
        else throw IOException("Failed to cast $term to Struct.")
    }
}
