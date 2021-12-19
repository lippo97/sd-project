package it.unibo.lpaas.delivery.http.databind

import com.fasterxml.jackson.databind.ObjectMapper
import it.unibo.lpaas.domain.databind.ObjectMappers

/**
 * A [BufferSerializer] that exposes the inner [ObjectMapper] instance.
 * Using this API the serializer can be configured after it was created,
 * making it easier to create some default serializer that can be extended
 * later.
 */
interface ObjectMapperSerializer : BufferSerializer {

    val objectMapper: ObjectMapper

    companion object {
        fun of(objectMapper: ObjectMapper): ObjectMapperSerializer = SimpleObjectMapperSerializer(objectMapper)

        fun json(): ObjectMapperSerializer = of(ObjectMappers.json())

        fun yaml(): ObjectMapperSerializer = of(ObjectMappers.yaml())

        fun xml(): ObjectMapperSerializer = of(ObjectMappers.xml())
    }
}
