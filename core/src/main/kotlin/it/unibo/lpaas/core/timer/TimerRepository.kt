package it.unibo.lpaas.core.timer

import it.unibo.lpaas.core.persistence.repository.Create
import it.unibo.lpaas.core.persistence.repository.DeleteByName
import it.unibo.lpaas.core.persistence.repository.FindByName
import it.unibo.lpaas.core.persistence.repository.Repository
import it.unibo.lpaas.domain.SolutionId

interface TimerRepository<TimerID> :
    FindByName<SolutionId, TimerID>,
    Create<SolutionId, TimerID, TimerID>,
    DeleteByName<SolutionId, TimerID> {

    companion object {
        fun <TimerID> of(repository: Repository<SolutionId, TimerID, TimerID>): TimerRepository<TimerID> =
            object : TimerRepository<TimerID> {
                override suspend fun findByName(name: SolutionId): TimerID =
                    repository.findByName(name)

                override suspend fun create(name: SolutionId, data: TimerID): TimerID =
                    repository.create(name, data)

                override suspend fun deleteByName(name: SolutionId): TimerID =
                    repository.deleteByName(name)
            }
    }
}
