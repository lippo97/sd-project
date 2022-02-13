package it.unibo.lpaas.http.databind

import io.vertx.core.buffer.Buffer
import it.unibo.lpaas.domain.databind.Deserializer
import it.unibo.lpaas.domain.databind.SerializationException

interface BufferDeserializer : Deserializer {
    @Throws(SerializationException::class)
    fun <T> decodeValue(buffer: Buffer, clazz: Class<T>): T

    companion object {
        fun of(deserializer: Deserializer): BufferDeserializer = SimpleBufferDeserializer(deserializer)
    }
}
