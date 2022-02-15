package it.unibo.lpaas

import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.ObjectMapperSerializer
import it.unibo.lpaas.http.databind.SerializerCollection
import it.unibo.lpaas.http.databind.SerializerConfiguration

object Serializers {
    val serializerCollection: SerializerCollection<ObjectMapperSerializer> = SerializerCollection.default().also {
        SerializerConfiguration.defaultWithModule {
            addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
            addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
            addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
            addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
        }
            .applyOnJacksonAndSerializers(it)
    }
}
