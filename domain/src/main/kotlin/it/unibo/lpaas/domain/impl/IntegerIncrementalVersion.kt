package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Version

/**
 * A non-negative progressive integer representing the version of the resource.
 */
class IntegerIncrementalVersion private constructor (val value: Int) : IncrementalVersion {
    override fun next(): IncrementalVersion = IntegerIncrementalVersion(value + 1)

    override fun compareTo(other: Version): Int {
        if (other !is IntegerIncrementalVersion) {
            throw IllegalArgumentException("Passed argument was not a SemanticVersion.")
        }
        return value.compareTo(other.value)
    }

    override fun equals(other: Any?): Boolean {
        if (other is IntegerIncrementalVersion) {
            return value == other.value
        }
        return false
    }

    override fun hashCode(): Int = value

    companion object {
        @Throws(IllegalArgumentException::class)
        fun unsafeMake(value: Int): IntegerIncrementalVersion {
            require(value >= 0)
            return IntegerIncrementalVersion(value)
        }

        val zero: IntegerIncrementalVersion = IntegerIncrementalVersion(0)
    }
}