package it.unibo.lpaas.domain

import java.time.Instant
import it.unibo.tuprolog.theory.Theory as Theory2P

data class Theory(
    val name: TheoryId,
    val value: Theory2P,
    val version: Version,
    val createdAt: Instant = Instant.now(),
)
