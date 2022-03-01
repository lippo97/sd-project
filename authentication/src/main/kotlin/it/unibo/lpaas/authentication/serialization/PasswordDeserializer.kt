package it.unibo.lpaas.authentication.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.authentication.domain.Password

class PasswordDeserializer : JsonDeserializer<Password>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Password =
        Password(StringDeserializer().deserialize(p, ctxt))
}
