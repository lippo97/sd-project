package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * An object that is capable of serializing objects to a [String].
 *
 * It is meant to be a stateful component, meaning its behaviour can change.
 * This allows [Serializer]s to be configured after their creation.
 */
interface Serializer {

    /**
     * Serialize an object of type [T] to its [String] representation.
     * @note: This method should be implemented as a pure function.
     */
    @Throws(SerializationException::class)
    fun <T> serializeToString(t: T): String

    companion object {
        fun of(objectMapper: ObjectMapper): Serializer = object : Serializer {
            override fun <T> serializeToString(t: T): String =
                runCatching {
                    objectMapper.writeValueAsString(t)
                }.recoverCatching {
                    when (it) {
                        is JsonProcessingException -> throw SerializationException(it.message, it.cause)
                        else -> throw it
                    }
                }
                    .getOrThrow()
        }
    }
}
