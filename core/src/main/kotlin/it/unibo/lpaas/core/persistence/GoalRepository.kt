package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId

typealias GoalRepository = Repository<GoalId, Goal.Data, Goal>
