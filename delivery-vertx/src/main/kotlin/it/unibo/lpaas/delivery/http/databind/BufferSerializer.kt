package it.unibo.lpaas.delivery.http.databind

import io.vertx.core.buffer.Buffer
import it.unibo.lpaas.domain.databind.SerializationException
import it.unibo.lpaas.domain.databind.Serializer

/**
 * An extension of [Serializer] that is capable of serializing objects to a
 * [Buffer].
 */
interface BufferSerializer : Serializer {
    @Throws(SerializationException::class)
    fun <T> serializeToBuffer(t: T): Buffer
}
