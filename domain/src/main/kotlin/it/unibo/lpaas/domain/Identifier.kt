package it.unibo.lpaas.domain

/**
 * A generic identifier for resources.
 */
interface Identifier {
    /**
     * Returns a human-readable representation of itself.
     */
    fun show(): String
}
