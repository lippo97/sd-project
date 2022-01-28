package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.repository.Create
import it.unibo.lpaas.core.persistence.repository.DeleteByName
import it.unibo.lpaas.core.persistence.repository.FindByName
import it.unibo.lpaas.core.persistence.repository.UpdateByName
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId

interface SolutionRepository :
    FindByName<SolutionId, Solution>,
    Create<SolutionId, Solution.Data, Solution>,
    DeleteByName<SolutionId, Solution>,
    UpdateByName<SolutionId, Solution.Data, Solution> {
    @Throws(NotFoundException::class)
    suspend fun findByNameAndVersion(name: SolutionId, version: IncrementalVersion): Solution
}
