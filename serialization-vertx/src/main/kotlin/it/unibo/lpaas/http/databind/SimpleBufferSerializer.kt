package it.unibo.lpaas.http.databind

import io.vertx.core.buffer.Buffer
import it.unibo.lpaas.domain.databind.Serializer

class SimpleBufferSerializer(serializer: Serializer) : BufferSerializer, Serializer by serializer {
    override fun <T> serializeToBuffer(t: T): Buffer =
        Buffer.buffer(serializeToString(t))
}
