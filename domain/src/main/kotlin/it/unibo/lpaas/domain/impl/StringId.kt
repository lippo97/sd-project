package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import java.util.UUID

data class StringId(val value: String) : GoalId, TheoryId, SolutionId {
    override fun show(): String = value

    companion object {
        fun uuid(): StringId = StringId(UUID.randomUUID().toString())
    }
}
