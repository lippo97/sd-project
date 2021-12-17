package it.unibo.lpaas.delivery.http.databind

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import io.vertx.core.buffer.Buffer
import it.unibo.lpaas.domain.databind.SerializationException
import it.unibo.lpaas.domain.databind.Serializer

interface BufferSerializer : Serializer {
    @Throws(SerializationException::class)
    fun <T> serializeToBuffer(t: T): Buffer

    companion object {
        fun of(serializer: Serializer): BufferSerializer = SimpleBufferSerializer(serializer)

        fun of(objectWriter: ObjectWriter): BufferSerializer = of(Serializer.of(objectWriter))

        fun of(objectMapper: ObjectMapper): BufferSerializer = of(objectMapper.writer())
    }
}
