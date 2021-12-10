package it.unibo.lpaas.domain

import java.time.Instant

interface Fact {

    val version: Version

    val createdAt: Instant

    val value: Prolog

    companion object {

        @JvmStatic
        fun of(
            version: Version,
            timestamp: Instant,
            value: Prolog
        ): Fact = TODO()
    }
}
