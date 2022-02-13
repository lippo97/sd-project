package it.unibo.lpaas.delivery.http

import io.vertx.core.Vertx
import it.unibo.lpaas.core.Generator
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.core.timer.Timer
import it.unibo.lpaas.core.timer.TimerRepository
import it.unibo.lpaas.delivery.StringParser
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.http.databind.BufferSerializer
import it.unibo.lpaas.http.databind.SerializerCollection
import it.unibo.tuprolog.solve.SolverFactory

data class GoalDependencies(
    val goalRepository: GoalRepository,
    val goalIdParser: StringParser<GoalId>,
)

data class TheoryDependencies(
    val theoryRepository: TheoryRepository,
    val theoryIdParser: StringParser<TheoryId>,
    val functorParser: StringParser<Functor>,
    val incrementalVersionParser: StringParser<IncrementalVersion>,
)

data class SolutionDependencies(
    val solutionRepository: SolutionRepository,
    val solutionIdParser: StringParser<SolutionId>,
    val solutionIdGenerator: Generator<SolutionId>,
    val solverFactory: SolverFactory,
)

data class TimerDependencies<TimerID>(
    val timerRepository: TimerRepository<TimerID>,
    val timer: Timer<TimerID>,
)

data class DependencyGraph<TimerID>(
    val vertx: Vertx,
    val timerDependencies: TimerDependencies<TimerID>,
    val serializerCollection: SerializerCollection<BufferSerializer>,
    val goalDependencies: GoalDependencies,
    val theoryDependencies: TheoryDependencies,
    val solutionDependencies: SolutionDependencies,
    val authOptions: Controller.AuthOptions,
)
