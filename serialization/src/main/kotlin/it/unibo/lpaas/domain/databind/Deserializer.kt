package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.core.exc.StreamWriteException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.ObjectReader
import java.io.IOException
import java.io.StringWriter

interface Deserializer {

    @Throws(SerializationException::class)
    fun <T> decodeValue(string: String, clazz: Class<T>): T

    companion object {
        fun of(objectReader: ObjectReader): Deserializer = object : Deserializer {
            override fun <T> decodeValue(string: String, clazz: Class<T>): T =
                runCatching {
                    objectReader.readValue(string, clazz)
                }.recoverCatching {
                    if (it is IOException) throw SerializationException(it.message, it.cause)
                    else throw it
                }.getOrThrow()
        }
    }
}
