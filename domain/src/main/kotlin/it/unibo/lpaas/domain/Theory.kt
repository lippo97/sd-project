package it.unibo.lpaas.domain

import java.time.Instant
import it.unibo.tuprolog.theory.Theory as Theory2P

data class Theory(
    val name: TheoryId,
    val data: Data,
    val version: Version,
    val createdAt: Instant = Instant.now(),
) {
    data class Data(val value: Theory2P) {

    }

}
