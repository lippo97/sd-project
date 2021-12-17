package it.unibo.lpaas.domain.databind.impl

import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import it.unibo.lpaas.domain.databind.derive
import it.unibo.lpaas.domain.impl.StringId

val stringIdDeserializer by lazy { StringDeserializer().derive { StringId(it) } }
