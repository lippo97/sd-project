package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.IncrementalVersion
import it.unibo.lpaas.domain.impl.SemanticVersion

interface Version : Comparable<Version> {
    companion object {
        @JvmStatic
        fun incremental(startingAt: Int = 0): Version? = IncrementalVersion.of(startingAt)

        @JvmStatic
        val incremental: Version = IncrementalVersion.of(0)!!

        @JvmStatic
        fun semantic(x: Int, y: Int, z: Int): Version? = SemanticVersion.of(x, y, z)
    }
}
