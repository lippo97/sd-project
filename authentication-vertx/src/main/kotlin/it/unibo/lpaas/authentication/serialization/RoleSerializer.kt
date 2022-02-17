package it.unibo.lpaas.authentication.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.auth.Role

class RoleSerializer : JsonSerializer<Role>() {
    override fun serialize(role: Role, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(role.value)
    }
}
