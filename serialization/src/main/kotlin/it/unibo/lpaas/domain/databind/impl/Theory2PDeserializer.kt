package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.domain.databind.derive
import it.unibo.tuprolog.theory.parsing.ClausesParser

val theory2PDeserializer = StringDeserializer().derive {
    ClausesParser.withDefaultOperators.parseTheory(it)
}
