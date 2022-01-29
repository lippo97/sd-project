package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.timer.TimerRepository
import it.unibo.lpaas.domain.SolutionId

class InMemoryTimerRepository<TimerID : Any>(
    memory: Map<SolutionId, TimerID> = mapOf()
) : TimerRepository<TimerID> by TimerRepository.of(
    BaseMemoryRepository(
        memory,
        "Timer",
        { _, v -> v },
    )
)

fun <TimerID : Any> TimerRepository.Companion.inMemory(memory: Map<SolutionId, TimerID> = mapOf()) =
    InMemoryTimerRepository(memory)
