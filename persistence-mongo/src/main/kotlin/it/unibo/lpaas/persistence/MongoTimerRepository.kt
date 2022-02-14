package it.unibo.lpaas.persistence

import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.timer.TimerRepository
import it.unibo.lpaas.domain.SolutionId
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

data class TimerRecord<TimerID>(val name: SolutionId, val timerId: TimerID)

class MongoTimerRepository<TimerID : Any> (
    private val timerCollection: CoroutineCollection<TimerRecord<TimerID>>,
) : TimerRepository<TimerID> {

    override suspend fun create(name: SolutionId, data: TimerID): TimerID =
        TimerRecord(name, data).also {
            timerCollection.insertOne(it)
        }.timerId

    override suspend fun findByName(name: SolutionId): TimerID =
        timerCollection.findOne(TimerRecord<TimerID>::name eq name)?.timerId
            ?: throw NotFoundException(name, "Timer")

    override suspend fun deleteByName(name: SolutionId): TimerID =
        timerCollection.findOneAndDelete(TimerRecord<TimerID>::name eq name)?.timerId
            ?: throw NotFoundException(name, "Timer")
}

fun <TimerID : Any> TimerRepository.Companion.mongo(
    timerCollection: CoroutineCollection<TimerRecord<TimerID>>
): TimerRepository<TimerID> =
    MongoTimerRepository(timerCollection)
