package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Version

/**
 * A non-negative progressive integer representing the version of the resource.
 */
@JvmInline
value class IncrementalVersionImpl private constructor (val value: Int) : IncrementalVersion {
    override fun next(): IncrementalVersion = IncrementalVersionImpl(value + 1)

    override fun compareTo(other: Version): Int {
        if (other !is IncrementalVersionImpl) {
            throw IllegalArgumentException("Passed argument was not a SemanticVersion.")
        }
        return value.compareTo(other.value)
    }

    companion object {
        @Throws(IllegalArgumentException::class)
        fun unsafeMake(value: Int): IncrementalVersion {
            require(value >= 0)
            return IncrementalVersionImpl(value)
        }

        val zero: IncrementalVersion = IncrementalVersionImpl(0)
    }
}
