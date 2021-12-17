package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.TheoryId

data class StringId(val name: String) : GoalId, TheoryId {
    override fun show(): String = name
}
