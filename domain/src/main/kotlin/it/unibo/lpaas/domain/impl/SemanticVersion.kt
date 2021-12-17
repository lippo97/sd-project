@file:Suppress("DataClassPrivateConstructor")

package it.unibo.lpaas.domain.impl

import it.unibo.lpaas.domain.Version

/**
 * Just a simple representation inspired by the semantic versioning system.
 * It just supports a version expressed in the for `X.Y.Z`.
 */
data class SemanticVersion private constructor (val x: Int, val y: Int, val z: Int) : Version {

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

    companion object {
        fun of(x: Int, y: Int, z: Int): Version? =
            SemanticVersion(x, y, z).takeUnless { listOf(x, y, z).any { it < 0 } }
    }
}
