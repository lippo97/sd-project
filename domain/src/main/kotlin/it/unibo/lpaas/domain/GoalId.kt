package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.StringId

interface GoalId : Identifier {
    companion object {

        @JvmStatic
        fun of(name: String): GoalId = StringId(name)
    }
}
