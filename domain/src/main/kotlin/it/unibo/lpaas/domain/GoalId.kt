package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.StringId

interface GoalId : Identifier {
    companion object {
        fun of(name: String): GoalId = StringId(name)
    }
}
