package it.unibo.lpaas.domain

/**
 * A human-readable piece of data.
 */
interface Printable {
    /**
     * Returns a human-readable representation of itself.
     */
    fun show(): String
}
