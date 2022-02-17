package it.unibo.lpaas.client.repl

import io.vertx.core.Future
import io.vertx.core.Vertx
import it.unibo.lpaas.client.Formatter
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.client.repl.console.Console
import it.unibo.lpaas.client.repl.console.EnhancedConsole
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.tuprolog.core.parsing.ParseException
import java.util.UUID
import it.unibo.tuprolog.theory.Theory as Theory2P

class LpaasRepl private constructor (
    vertx: Vertx,
    private val lpaas: Lpaas,
    private val theoryId: TheoryId,
    private val resultFormatter: Formatter<Result>,
    parseExceptionFormatter: Formatter<ParseException>,
    private val sessionId: String,
) : Console by Console.standard(vertx.createSharedWorkerExecutor("console", 1)),
    EnhancedConsole(parseExceptionFormatter) {

    private var currentRound: Int = 0

    private val currentGoalId: GoalId
        get() = GoalId.of("goal-$sessionId-$currentRound")

    private val currentSolutionId: SolutionId
        get() = SolutionId.of("solution-$sessionId-$currentRound")

    fun repl(): Future<Void> =
        shellReadStruct()
            .map { listOf(Subgoal(it)) }
            .flatMap {
                lpaas.createGoal(currentGoalId, Goal.Data(it))
            }
            .flatMap {
                lpaas.createSolution(
                    currentSolutionId,
                    Solution.Data(
                        theoryOptions = Solution.TheoryOptions(
                            name = theoryId,
                        ),
                        it.name,
                    )
                )
            }
            .flatMap {
                lpaas.getResults(it.name)
            }
            .onSuccess { res ->
                ResultsHandler(res)
                    .onResult { putStrLn(resultFormatter.format(it)) }
                    .onHasNext {
                        readLine().flatMap {
                            if (it == ";") res.next()
                            else repl()
                        }
                    }
                    .onEnd { repl() }
            }
            .flatMap { it.next() }
            .onSuccess { currentRound++ }
            .map { null }

    companion object {
        @Suppress("LongParameterList")
        fun fromExistingTheory(
            vertx: Vertx,
            lpaas: Lpaas,
            theoryId: TheoryId,
            resultFormatter: Formatter<Result> = Formatter.result,
            parseExceptionFormatter: Formatter<ParseException> = Formatter.parseException,
            sessionId: String = UUID.randomUUID().toString()
        ): Future<LpaasRepl> =
            lpaas.getTheoryByName(theoryId)
                .map {
                    LpaasRepl(
                        vertx,
                        lpaas,
                        theoryId,
                        resultFormatter,
                        parseExceptionFormatter,
                        sessionId
                    )
                }

        @Suppress("LongParameterList")
        fun fromTheory(
            vertx: Vertx,
            lpaas: Lpaas,
            theory2p: Theory2P,
            resultFormatter: Formatter<Result> = Formatter.result,
            parseExceptionFormatter: Formatter<ParseException> = Formatter.parseException,
            sessionId: String = UUID.randomUUID().toString()
        ): Future<LpaasRepl> =
            lpaas.createTheory(StringId.uuid(), Theory.Data(theory2p))
                .map { it.name }
                .flatMap { fromExistingTheory(vertx, lpaas, it, resultFormatter, parseExceptionFormatter, sessionId) }
    }
}
