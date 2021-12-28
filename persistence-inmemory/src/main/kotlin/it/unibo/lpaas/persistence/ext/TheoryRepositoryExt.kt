package it.unibo.lpaas.persistence.ext

import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.persistence.InMemoryTheoryRepository

fun TheoryRepository.Companion.inMemory(
    memory: Map<TheoryId, List<Theory>> = mapOf(),
): TheoryRepository =
    InMemoryTheoryRepository(memory)
