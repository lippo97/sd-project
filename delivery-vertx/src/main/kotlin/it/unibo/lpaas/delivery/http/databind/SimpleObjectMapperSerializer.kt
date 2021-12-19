package it.unibo.lpaas.delivery.http.databind

import com.fasterxml.jackson.databind.ObjectMapper
import it.unibo.lpaas.domain.databind.Serializer

class SimpleObjectMapperSerializer(
    override val objectMapper: ObjectMapper
) : ObjectMapperSerializer,
    BufferSerializer by SimpleBufferSerializer(Serializer.of(objectMapper))
