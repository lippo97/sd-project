package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.StringId

interface TheoryId : Identifier {
    companion object {
        fun of(name: String): TheoryId = StringId(name)
    }
}
