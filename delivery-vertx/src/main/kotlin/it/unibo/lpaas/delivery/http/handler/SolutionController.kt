package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.lpaas.core.Generator
import it.unibo.lpaas.core.GetResultsOptions
import it.unibo.lpaas.core.SolutionUseCases
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.core.timer.Timer
import it.unibo.lpaas.core.timer.TimerRepository
import it.unibo.lpaas.delivery.StringParser
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.MimeType
import it.unibo.lpaas.delivery.http.databind.SerializerCollection
import it.unibo.lpaas.delivery.http.handler.dsl.HandlerDSL
import it.unibo.lpaas.delivery.http.handler.dto.CreateSolutionDTO
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.SolutionId
import it.unibo.tuprolog.solve.SolverFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.jvm.JvmStatic
import kotlin.time.Duration

interface SolutionController : Controller {

    companion object {

        @JvmStatic
        @Suppress("LongParameterList")
        fun <TimerID> make(
            vertx: Vertx,
            goalRepository: GoalRepository,
            theoryRepository: TheoryRepository,
            solutionRepository: SolutionRepository,
            timerRepository: TimerRepository<TimerID>,
            solutionIdParser: StringParser<SolutionId>,
            incrementalVersionParser: StringParser<IncrementalVersion>,
            timer: Timer<TimerID>,
            solutionIdGenerator: Generator<SolutionId>,
            authOptions: Controller.AuthOptions,
            serializerCollection: SerializerCollection<BufferSerializer>,
            solverFactory: SolverFactory,
        ): SolutionController = object : SolutionController {

            val solutionUseCases = SolutionUseCases(
                goalRepository,
                theoryRepository,
                solutionRepository,
                timerRepository,
                timer,
                solutionIdGenerator,
            )
            @Suppress("LongMethod")
            override fun routes(): Router = Router.router(vertx).apply {
                with(
                    HandlerDSL(
                        serializerCollection,
                        authOptions.authenticationHandler,
                        authOptions.authorizationProvider
                    )
                ) {

                    post("/")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(SolutionUseCases.Tags.createSolution)
                        .handler(BodyHandler.create())
                        .dataHandler(HTTPStatusCode.CREATED) { ctx ->
                            val every = ctx.queryParam("every")
                                .map(Duration::parse)
                                .getOrNull(0)
                            val (name, data) = decodeJson(ctx.body, CreateSolutionDTO::class.java)
                            solutionUseCases.createSolution(name, data, every)
                        }

                    get("/:name")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(SolutionUseCases.Tags.getSolution)
                        .dataHandler { ctx ->
                            val name = ctx.pathParam("name")
                            solutionUseCases.getSolution(solutionIdParser.parse(name))
                        }

                    get("/:name/history/:version")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(SolutionUseCases.Tags.getSolutionByVersion)
                        .dataHandler { ctx ->
                            val name = ctx.pathParam("name")
                            val version = incrementalVersionParser.parse(ctx.pathParam("versionOrTimestamp"))
                            solutionUseCases.getSolutionByVersion(
                                solutionIdParser.parse(name),
                                version,
                            )
                        }

                    get("/:name/results")
                        .handler { ctx ->
                            val name = solutionIdParser.parse(ctx.pathParam("name"))
                            val skip = ctx.queryParam("skip")
                                .map(Integer::parseInt)
                                .getOrNull(0)
                            val limit = ctx.queryParam("limit")
                                .map(Integer::parseInt)
                                .getOrNull(0)
                            val within = ctx.queryParam("within")
                                .map(Duration::parse)
                                .getOrNull(0)
                            val mimeType = MimeType.safeParse(ctx.acceptableContentType ?: "") ?: MimeType.JSON

                            val serializer = serializerCollection.serializerForMimeType(mimeType)
                                ?: error("Couldn't find serializer for $mimeType.")
                            val fws = ctx.request().toWebSocket()
                            fws
                                .onSuccess { ws ->
                                    GlobalScope.launch(vertx.dispatcher()) {
                                        val solutions = solutionUseCases.getResults(
                                            name, solverFactory,
                                            GetResultsOptions(
                                                skip,
                                                limit,
                                                within,
                                            )
                                        )
                                            .iterator()

                                        ws.onMessage("get") {
                                            val next = if (solutions.hasNext()) solutions.next() else null
                                            if (next != null) {
                                                ws.write(serializer.serializeToBuffer(next))
                                            }
                                            if (next == null || next is Result.No || next is Result.Halt) {
                                                ws.close()
                                                    .onSuccess { println("Connection closed") }
                                            }
                                        }
                                    }
                                }
                                .onFailure { fail ->
                                    fail.printStackTrace()
                                    ctx.fail(HTTPStatusCode.BAD_REQUEST.code)
                                }
                        }

                    delete("/:name")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(SolutionUseCases.Tags.deleteByName)
                        .dataHandler(HTTPStatusCode.NO_CONTENT) { ctx ->
                            val name = ctx.pathParam("name")
                            solutionUseCases.deleteSolution(solutionIdParser.parse(name)).void()
                        }
                }
            }
        }
    }
}
