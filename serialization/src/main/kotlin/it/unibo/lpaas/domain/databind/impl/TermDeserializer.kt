package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.domain.databind.derive
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.TermParser

val termDeserializer: JsonDeserializer<Term> by lazy {
    StringDeserializer().derive {
        TermParser.withStandardOperators.parseTerm(it)
    }
}
