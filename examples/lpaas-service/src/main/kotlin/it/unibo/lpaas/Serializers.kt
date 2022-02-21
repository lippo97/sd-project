package it.unibo.lpaas

import it.unibo.lpaas.http.databind.ObjectMapperSerializer
import it.unibo.lpaas.http.databind.SerializerCollection
import it.unibo.lpaas.http.databind.SerializerConfiguration

object Serializers {
    val serializerCollection: SerializerCollection<ObjectMapperSerializer> = SerializerCollection.default().also {
        SerializerConfiguration.defaultWithModule {
            addDomainTypeMappings()
        }
            .applyOnJacksonAndSerializers(it)
    }
}
