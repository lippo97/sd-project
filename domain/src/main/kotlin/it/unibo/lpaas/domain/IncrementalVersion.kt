package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion

interface IncrementalVersion : Version {

    fun next(): IncrementalVersion

    companion object {

        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun unsafeMakeInteger(value: Int): IntegerIncrementalVersion = IntegerIncrementalVersion.unsafeMake(value)

        @JvmStatic
        fun of(value: Int): IntegerIncrementalVersion? =
            runCatching { unsafeMakeInteger(value) }
                .getOrNull()

        @JvmStatic
        val zero: IncrementalVersion = IntegerIncrementalVersion.zero
    }
}
