package it.unibo.lpaas.authentication.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.auth.Role

class RoleDeserializer : JsonDeserializer<Role>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Role =
        Role.parse(StringDeserializer().deserialize(p, ctxt))
}
