@file:Suppress("DataClassPrivateConstructor")

package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Version

/**
 * Just a simple representation inspired by the semantic versioning system.
 * It just supports a version expressed in the for `X.Y.Z`.
 */
class SemanticVersion private constructor (val x: Int, val y: Int, val z: Int) : Version {

    override fun compareTo(other: Version): Int {
        if (other !is SemanticVersion) {
            throw IllegalArgumentException("Passed argument was not a SemanticVersion.")
        }
        return cascadeCompare(other)
    }

    private fun cascadeCompare(other: SemanticVersion): Int = listOf(
        x.compareTo(other.x),
        y.compareTo(other.y),
        z.compareTo(other.z),
    ).firstOrNull { it != 0 } ?: 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SemanticVersion

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }

    companion object {
        fun of(x: Int, y: Int, z: Int): SemanticVersion? =
            SemanticVersion(x, y, z).takeUnless { listOf(x, y, z).any { it < 0 } }
    }
}
