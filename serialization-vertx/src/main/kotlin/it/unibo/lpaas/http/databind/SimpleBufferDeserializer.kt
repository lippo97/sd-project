package it.unibo.lpaas.http.databind

import io.vertx.core.buffer.Buffer
import it.unibo.lpaas.domain.databind.Deserializer

class SimpleBufferDeserializer(deserializer: Deserializer) : BufferDeserializer, Deserializer by deserializer {
    override fun <T> decodeValue(buffer: Buffer, clazz: Class<T>): T = decodeValue(buffer.toString(), clazz)
}
