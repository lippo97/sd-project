package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.databind.Printer
import it.unibo.tuprolog.core.Clause

class FactSerializer(
    private val clausePrinter: Printer<Clause>
) : JsonSerializer<Fact>() {
    override fun serialize(value: Fact, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(clausePrinter.display(value.fact))
    }
}
