package it.unibo.lpaas.client.mio

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.streams.ReadStream
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.TheoryId
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.ParseException
import it.unibo.tuprolog.core.parsing.parse
import java.util.LinkedList
import java.util.UUID

class AsyncRepl2(
    private val vertx: Vertx,
    private val lpaas: Lpaas,
    private val theoryId: TheoryId,
    private val resultFormatter: Formatter<Result>,
    private val parseExceptionFormatter: Formatter<ParseException>,
    private val sessionId: String,
) {

    private var currentRound: Int = 0

    private val outputWorkerPool = vertx.createSharedWorkerExecutor("output", 1)

    private fun putStr(out: String): Future<Void> = outputWorkerPool.executeBlocking { print(out); it.complete() }

    private fun putStrLn(out: String): Future<Void> = outputWorkerPool.executeBlocking { println(out); it.complete() }

    private fun readLine(): Future<String> =
        outputWorkerPool.executeBlocking { it.complete(kotlin.io.readLine()) }

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

    fun repl(): Future<Void> =
        shellReadStruct()
            .map { listOf(Subgoal(it)) }
            .flatMap {
                lpaas.createGoal(GoalId.of("replGoal-$sessionId-$currentRound"), Goal.Data(it))
            }
            .flatMap {
                lpaas.createSolution(
                    SolutionId.of("replSolution-$sessionId-$currentRound"),
                    Solution.Data(
                        theoryOptions = Solution.TheoryOptions(
                            name = theoryId,
                        ),
                        GoalId.of("replGoal-$sessionId-$currentRound")
                    )
                )
            }
            .flatMap {
                val (stream, next) = lpaas.getResults(it.name)
//                val solutionsStream = MyReadStream(stream)
//                solutionsStream.onResult { res ->
//                    putStrLn(resultFormatter.format(res))
//                        .flatMap {
//                            if (solutionsStream.hasNext()) {
//                                readLine().flatMap { input ->
//                                    if (input == ";") next()
//                                    else Future.succeededFuture()
//                                }
//                            } else repl()
//                        }

                ResultsHandler(stream, next)
                    .onResult { putStrLn(resultFormatter.format(it)) }
                    .onHasNext {
                        readLine().flatMap { input ->
                            if (input == ";") next()
                            else repl()
                        }
                    }
                    .onEnd { repl() }

                next()
            }
            .onSuccess { currentRound++ }

//    class MyReadStream(val stream: ReadStream<Result>) {
//        private val solutions = mutableListOf<Result?>()
//
//        fun onResult(handler: Handler<Result>) {
//            stream.handler {
//                solutions.add(it)
//                if (solutions.size > 1) handler.handle(solutions.removeFirst())
//            }
//        }
//
//        fun hasNext(): Boolean = solutions.firstOrNull() != null
//    }

    class ResultsHandler(
        private val stream: ReadStream<Result>,
        private val next: () -> Future<Void>
    ) {
        private val solutions = LinkedList<Result?>()

        private var onNextHandler: Handler<Void>? = null
        private var onEndHandler: Handler<Void>? = null

        fun onResult(handler: Handler<Result>): ResultsHandler = apply {
            stream.handler {
                solutions.add(it)
                if (solutions.size > 1) {
                    handler.handle(solutions.removeFirst())
                    if (hasNext()) onNextHandler?.handle(null)
                    else onEndHandler?.handle(null)
                } else if (hasNext()) {
                    next()
                }
            }
        }

        fun onHasNext(handler: Handler<Void>): ResultsHandler = apply {
            onNextHandler = handler
        }

        fun onEnd(handler: Handler<Void>): ResultsHandler = apply {
            onEndHandler = handler
        }

        private fun hasNext(): Boolean = solutions.firstOrNull() != null
    }

    companion object {
        @Suppress("LongParameterList")
        fun fromExistingTheory(
            vertx: Vertx,
            lpaas: Lpaas,
            theoryId: TheoryId,
            resultFormatter: Formatter<Result> = Formatter.result,
            parseExceptionFormatter: Formatter<ParseException> = Formatter.parseException,
            sessionId: String = UUID.randomUUID().toString()
        ): Future<AsyncRepl2> =
            Future.succeededFuture(
                AsyncRepl2(
                    vertx,
                    lpaas,
                    theoryId,
                    resultFormatter,
                    parseExceptionFormatter,
                    sessionId
                )
            )
    }
}
