package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.domain.Variable
import it.unibo.lpaas.domain.databind.derive

val variableDeserializer by lazy {
    StringDeserializer()
        .derive { Variable(it) }
}

class VariableKeyDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String, ctxt: DeserializationContext): Variable =
        Variable(key)
}
