package it.unibo.lpaas.authentication.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.authentication.domain.Username

class UsernameDeserializer : JsonDeserializer<Username>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Username =
        Username(StringDeserializer().deserialize(p, ctxt))
}
