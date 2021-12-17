package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Version

/**
 * A non-negative progressive integer representing the version of the resource.
 */
class IncrementalVersion private constructor (val value: Int) : Version {
    override fun compareTo(other: Version): Int {
        if (other !is IncrementalVersion) {
            throw IllegalArgumentException("Passed argument was not a SemanticVersion.")
        }
        return value.compareTo(other.value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IncrementalVersion

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value

    companion object {
        fun of(value: Int): IncrementalVersion? =
            if (value >= 0) IncrementalVersion(value) else null
    }
}
