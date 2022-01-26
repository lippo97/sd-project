package it.unibo.lpaas.core.timer

import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.persistence.repository.FindByName
import it.unibo.lpaas.domain.SolutionId
import kotlin.jvm.Throws

interface TimerRepository<TimerID> : FindByName<SolutionId, List<TimerID>> {
    /**
     * @throws DuplicateIdentifierException if the timerId is already present
     * for the given id.
     */
    @Throws(DuplicateIdentifierException::class)
    suspend fun append(name: SolutionId, timerId: TimerID)
}
