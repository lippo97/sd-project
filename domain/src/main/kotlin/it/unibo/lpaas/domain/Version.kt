package it.unibo.lpaas.domain

/**
 * A non-negative progressive integer representing the version of the resource.
 */
@JvmInline
value class Version private constructor (val value: Int) {
    companion object {
        fun of(value: Int = 0): Version? =
            if (value >= 0) Version(value) else null
    }
}
