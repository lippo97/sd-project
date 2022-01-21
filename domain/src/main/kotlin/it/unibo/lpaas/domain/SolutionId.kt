package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.StringId

interface SolutionId : Identifier {
    companion object {

        @JvmStatic
        fun of(name: String): SolutionId = StringId(name)
    }
}
