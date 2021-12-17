package it.unibo.lpaas.domain.databind.impl

import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.databind.derive

val subgoalDeserializer by lazy {
    structDeserializer.derive { Subgoal(it) }
}
