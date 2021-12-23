package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

object ObjectMappers {
    fun json(): ObjectMapper = ObjectMapper(JsonFactory())
        .enable(SerializationFeature.INDENT_OUTPUT)

    fun yaml(): ObjectMapper = ObjectMapper(YAMLFactory())

    fun xml(): ObjectMapper = XmlMapper()
}
