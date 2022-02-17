package it.unibo.lpaas.authentication.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.authentication.provider.Username

class UsernameSerializer : JsonSerializer<Username>() {
    override fun serialize(username: Username, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(username.value)
    }
}
