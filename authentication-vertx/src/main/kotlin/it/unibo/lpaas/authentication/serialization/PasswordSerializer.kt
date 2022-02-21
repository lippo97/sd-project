package it.unibo.lpaas.authentication.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import it.unibo.lpaas.authentication.domain.Password

class PasswordSerializer : JsonSerializer<Password>() {
    override fun serialize(password: Password, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(password.value)
    }
}
