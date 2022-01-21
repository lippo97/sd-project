package it.unibo.lpaas.core.persistence

import it.unibo.lpaas.core.persistence.repository.Repository
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId

interface GoalRepository : Repository<GoalId, Goal.Data, Goal> {

    companion object {
        /**
         * Factory method for the creation of a [GoalRepository] from a
         * [Repository] by delegation.
         *
         * Unfortunately this is needed because [GoalRepository] isn't defined
         * by a `typealias` but as an extension of [Repository], so its
         * delegation using wouldn't be enough to fulfill the [GoalRepository]
         * interface contract.
         */
        @JvmStatic
        fun of(repository: Repository<GoalId, Goal.Data, Goal>): GoalRepository = object : GoalRepository {
            override suspend fun findAll(): List<Goal> = repository.findAll()

            override suspend fun findByName(name: GoalId): Goal = repository.findByName(name)

            override suspend fun create(name: GoalId, data: Goal.Data): Goal = repository.create(name, data)

            override suspend fun updateByName(name: GoalId, data: Goal.Data): Goal = repository.updateByName(name, data)

            override suspend fun deleteByName(name: GoalId): Goal = repository.deleteByName(name)
        }
    }
}
