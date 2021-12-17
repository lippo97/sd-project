package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectWriter

interface Serializer {

    @Throws(SerializationException::class)
    fun <T> serializeToString(t: T): String

    companion object {
        fun of(objectWriter: ObjectWriter): Serializer = object : Serializer {
            override fun <T> serializeToString(t: T): String =
                runCatching {
                    objectWriter.writeValueAsString(t)
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
