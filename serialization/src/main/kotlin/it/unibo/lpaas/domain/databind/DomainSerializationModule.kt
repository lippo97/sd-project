package it.unibo.lpaas.domain.databind

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleSerializers
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.databind.impl.FactSerializer
import it.unibo.lpaas.domain.databind.impl.StringIDSerializer
import it.unibo.lpaas.domain.databind.impl.StructToStringSerializer
import it.unibo.lpaas.domain.databind.impl.Theory2PSerializer
import it.unibo.lpaas.domain.databind.impl.factDeserializer
import it.unibo.lpaas.domain.databind.impl.stringIdDeserializer
import it.unibo.lpaas.domain.databind.impl.structDeserializer
import it.unibo.lpaas.domain.databind.impl.theory2PDeserializer
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.theory.Theory as Theory2P

class DomainSerializationModule : Module() {

    override fun version(): Version = Version.unknownVersion()

    override fun getModuleName(): String = "DomainSerializationModule"

    override fun setupModule(context: SetupContext) {
        context.apply {
            addSerializers(
                SimpleSerializers().apply {
                    addSerializer(Theory2P::class.java, Theory2PSerializer(Theory2PPrinter.prettyPrinter()))
                    addSerializer(Fact::class.java, FactSerializer())
                    addSerializer(StringId::class.java, StringIDSerializer())
                    addSerializer(Struct::class.java, StructToStringSerializer())
                }
            )
            addDeserializers(
                SimpleDeserializers().apply {
                    addDeserializer(Theory2P::class.java, theory2PDeserializer)
                    addDeserializer(Fact::class.java, factDeserializer)
                    addDeserializer(StringId::class.java, stringIdDeserializer)
                    addDeserializer(Struct::class.java, structDeserializer)
                }
            )
        }
    }
}
