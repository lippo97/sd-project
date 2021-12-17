package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.IntegerDeserializer
import it.unibo.lpaas.domain.databind.derive
import it.unibo.lpaas.domain.impl.IncrementalVersion
import java.io.IOException

val incrementalVersionDeserializer by lazy {
    IntegerDeserializer(Int::class.java, 0)
        .derive { IncrementalVersion.of(it) ?: throw IOException("Can't create IncrementalVersion of $it.") }
}
