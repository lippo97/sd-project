package it.unibo.lpaas.http.databind

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.json.jackson.DatabindCodec.mapper
import io.vertx.core.json.jackson.DatabindCodec.prettyMapper
import it.unibo.lpaas.domain.databind.DomainSerializationModule

/**
 * Configuration that can be applied on [ObjectMapper]s.
 * It exposes helper methods to apply the configuration to both the arguments
 * and the singleton Vertx [ObjectMapper] instances.
 */
fun interface SerializerConfiguration {
    fun applyOn(objectMappers: Collection<ObjectMapper>)

    fun applyOnJacksonAndObjectMappers(objectMappers: Collection<ObjectMapper>) =
        applyOn(objectMappers + listOf(mapper(), prettyMapper()))

    fun applyOnJacksonAndSerializers(objectMapperSerializers: SerializerCollection<ObjectMapperSerializer>) =
        applyOnJacksonAndObjectMappers(objectMapperSerializers.availableSerializers.map { it.objectMapper })

    companion object {

        fun default(abstractTypeMappingModule: Module? = null): SerializerConfiguration =
            SerializerConfiguration { oms ->
                oms.forEach { om ->
                    om.registerKotlinModule()
                    om.registerModule(JavaTimeModule())
                    om.registerModule(DomainSerializationModule())
                    abstractTypeMappingModule?.let {
                        om.registerModule(abstractTypeMappingModule)
                    }
                }
            }

        fun defaultWithModule(configure: SimpleModule.() -> Unit): SerializerConfiguration =
            with(SimpleModule()) {
                configure()
                default(this)
            }
    }
}
