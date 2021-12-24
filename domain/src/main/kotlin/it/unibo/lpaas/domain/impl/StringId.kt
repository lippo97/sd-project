package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.TheoryId

data class StringId(val value: String) : GoalId, TheoryId {
    override fun show(): String = value
}
