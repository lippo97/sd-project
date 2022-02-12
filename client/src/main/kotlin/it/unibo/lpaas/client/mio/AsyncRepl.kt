package it.unibo.lpaas.client.mio

import io.vertx.core.Future
import io.vertx.core.Vertx
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.TheoryId
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.format
import it.unibo.tuprolog.core.parsing.ParseException
import it.unibo.tuprolog.core.parsing.parse
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class AsyncRepl private constructor (
    private val vertx: Vertx,
    private val lpaas: Lpaas,
    private val theoryId: TheoryId,
    private val resultFormatter: Formatter<Result>,
    private val parseExceptionFormatter: Formatter<ParseException>,
) {

    private var currentRound: Int = 0

    val blockingQueue: BlockingQueue<Result?> = LinkedBlockingQueue()

    val writeWorkerPool = vertx.createSharedWorkerExecutor("writeWorkerPool")

    val readWorkerPool = vertx.createSharedWorkerExecutor("readWorkerPool", 2)

    private fun readLine(): Future<String> =
        vertx.executeBlocking { it.complete(kotlin.io.readLine()) }

    private fun shellReadLine(): Future<String> =
        putStr("?- ")
            .flatMap { readLine() }
            .flatMap { if (it == "") shellReadLine() else Future.succeededFuture(it) }

    private fun shellReadStruct(): Future<Struct> =
        shellReadLine()
            .flatMap {
                runCatching {
                    Struct.parse(it)
                }
                    .map { Future.succeededFuture(it) }
                    .recover {
                        if (it is ParseException)
                            putStrLn(parseExceptionFormatter.format(it))
                                .flatMap { shellReadStruct() }
                        else
                            throw it
                    }
                    .getOrThrow()
            }

    private fun putStr(out: String): Future<Void> = vertx.executeBlocking { print(out); it.complete() }

    private fun putStrLn(out: String): Future<Void> = vertx.executeBlocking { println(out); it.complete() }

    fun loop(): Future<Void> {

        fun exploreResults(next: () -> Future<Void>): Future<Void> =
            Future.succeededFuture<Void>()
                .flatMap {
                    readWorkerPool.executeBlocking<Result?> {
//                        val result = blockingQueue.take()
                        it.complete(Result.No(Struct.of("ciao")))
                    }
                        .flatMap { res ->
                            putStrLn(resultFormatter.format(res))
                        }
                        .flatMap {
                            if (blockingQueue.peek() != null)
                                readLine().flatMap { input ->
                                    if (input != ";") next().flatMap { exploreResults(next) }
                                    else Future.succeededFuture()
                                }
                            else Future.succeededFuture()
                        }
                }

        return shellReadStruct()
            .map { listOf(Subgoal(it)) }
            .flatMap {
                lpaas.createGoal(GoalId.of("replGoal$currentRound"), Goal.Data(it))
            }
            .flatMap {
                lpaas.createSolution(
                    SolutionId.of("replSolution$currentRound"),
                    Solution.Data(
                        theoryOptions = Solution.TheoryOptions(
                            name = theoryId,
                        ),
                        GoalId.of("replGoal$currentRound")
                    )
                )
            }
            .onSuccess { currentRound++ }
            .flatMap {
                val (stream, next) = lpaas.getResults(it.name)
                stream.handler { res ->
                    println(res)
                    writeWorkerPool.executeBlocking<Void> {
                        println(Thread.currentThread().name)
                        blockingQueue.put(res)
                        if (res != null && blockingQueue.remainingCapacity() > 0) {
                            next()
                        }
                        it.complete()
                    }
                }
                next()
                    .flatMap {
                        exploreResults(next)
                    }
            }
            .flatMap { loop() }
    }

    companion object {
        fun fromExistingTheory(
            vertx: Vertx,
            lpaas: Lpaas,
            theoryId: TheoryId,
            resultFormatter: Formatter<Result> = Formatter.result,
            parseExceptionFormatter: Formatter<ParseException> = Formatter.parseException,
        ): Future<AsyncRepl> =
            Future.succeededFuture(AsyncRepl(vertx, lpaas, theoryId, resultFormatter, parseExceptionFormatter))
    }
}
