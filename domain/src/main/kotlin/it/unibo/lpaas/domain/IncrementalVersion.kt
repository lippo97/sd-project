package it.unibo.lpaas.domain

import it.unibo.lpaas.domain.impl.IncrementalVersionImpl

interface IncrementalVersion : Version {
    fun next(): IncrementalVersion

    companion object {

        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun unsafeMake(value: Int): IncrementalVersion = IncrementalVersionImpl.unsafeMake(value)

        @JvmStatic
        fun of(value: Int): IncrementalVersion? =
            runCatching { unsafeMake(value) }
                .getOrNull()

        @JvmStatic
        val zero: IncrementalVersion = IncrementalVersionImpl.zero
    }
}
