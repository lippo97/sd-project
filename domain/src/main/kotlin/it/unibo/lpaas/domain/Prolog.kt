package it.unibo.lpaas.domain

/**
 * A Prolog clause or fact.
 * @note: The validation must be performed before the creation of a [[Prolog]].
 */
interface Prolog {

    /**
     * The Prolog source code.
     */
    val value: String

    /**
     * Empty companion aimed at letting extensions be injected through extension methods
     */
    companion object
}
