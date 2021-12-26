package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.StringId

interface TheoryId : Identifier {
    companion object {

        @JvmStatic
        fun of(name: String): TheoryId = StringId(name)
    }
}
