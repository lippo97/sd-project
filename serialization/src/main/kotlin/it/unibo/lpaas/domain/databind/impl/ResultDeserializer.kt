package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Variable
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term

class ResultDeserializer : JsonDeserializer<Result>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Result {
        val node = p.codec.readTree<JsonNode>(p)
        val query = node.findValue("query")
            ?.traverse(p.codec)
            ?.readValueAs(Struct::class.java)
            ?: error("Field 'query' should be defined")
        val solvedQuery = node.get("solvedQuery")
            ?.traverse(p.codec)
            ?.readValueAs(Struct::class.java)
        val variables: Map<Variable, Term>? = node.findValue("variables")
            ?.traverse(p.codec)
            ?.readValueAs(object : TypeReference<Map<Variable, Term>>() {})

        val exception = node.findValue("exception")
            ?.traverse(p.codec)
            ?.readValueAs(Result.Error::class.java)

        return if (solvedQuery != null && variables != null)
            Result.Yes(query, solvedQuery, variables)
        else if (exception != null)
            Result.Halt(query, exception)
        else
            Result.No(query)
    }
}
