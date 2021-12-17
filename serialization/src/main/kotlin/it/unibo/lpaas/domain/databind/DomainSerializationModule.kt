package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleSerializers
import it.unibo.lpaas.domain.databind.impl.StringIDSerializer
import it.unibo.lpaas.domain.databind.impl.StructSerializer
import it.unibo.lpaas.domain.databind.impl.stringIdDeserializer
import it.unibo.lpaas.domain.databind.impl.structDeserializer
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.tuprolog.core.Struct

class DomainSerializationModule : Module() {

    override fun version(): Version = Version.unknownVersion()

    override fun getModuleName(): String = "DomainSerializationModule"

    override fun setupModule(context: SetupContext) {
        context.apply {
            addSerializers(
                SimpleSerializers().apply {
                    addSerializer(StringId::class.java, StringIDSerializer())
                    addSerializer(Struct::class.java, StructSerializer())
                }
            )
            addDeserializers(
                SimpleDeserializers().apply {
                    addDeserializer(StringId::class.java, stringIdDeserializer)
                    addDeserializer(Struct::class.java, structDeserializer)
                }
            )
        }
    }
}
