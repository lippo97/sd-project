package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.domain.databind.Printer
import it.unibo.tuprolog.theory.Theory as Theory2P

class Theory2PSerializer(private val theory2PPrinter: Printer<Theory2P>) : JsonSerializer<Theory2P>() {
    override fun serialize(value: Theory2P, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(theory2PPrinter.display(value))
    }
}
