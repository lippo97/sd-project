package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.persistence.repository.Create
import it.unibo.lpaas.core.persistence.repository.FindByName
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId

interface SolutionRepository :
    FindByName<SolutionId, Solution>,
    Create<SolutionId, Solution.Data, Solution>
