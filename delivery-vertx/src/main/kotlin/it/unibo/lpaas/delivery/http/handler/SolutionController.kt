package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.ServerWebSocket
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.core.GetResultsOptions
import it.unibo.lpaas.core.SolutionUseCases
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.delivery.StringParser
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.SolutionDependencies
import it.unibo.lpaas.delivery.http.TimerDependencies
import it.unibo.lpaas.delivery.http.handler.dsl.HandlerDSL
import it.unibo.lpaas.delivery.http.handler.dto.CreateSolutionDTO
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.http.databind.BufferSerializer
import it.unibo.lpaas.http.databind.MimeType
import it.unibo.lpaas.http.databind.SerializerCollection
import kotlinx.coroutines.runBlocking
import kotlin.jvm.JvmStatic
import kotlin.time.Duration

interface SolutionController : Controller {

    companion object {

        @Suppress("LongParameterList", "LongMethod")
        @JvmStatic
        fun <TimerID> make(
            vertx: Vertx,
            solutionDependencies: SolutionDependencies,
            timerDependencies: TimerDependencies<TimerID>,
            goalRepository: GoalRepository,
            theoryRepository: TheoryRepository,
            incrementalVersionParser: StringParser<IncrementalVersion>,
            authOptions: Controller.AuthOptions,
            serializers: SerializerCollection<BufferSerializer>,
        ): SolutionController {
            val (
                solutionRepository,
                solutionIdParser,
                solutionIdGenerator,
                solverFactory
            ) = solutionDependencies
            val (timerRepository, timer) = timerDependencies
            return object : SolutionController {
                val solutionUseCases = SolutionUseCases(
                    goalRepository,
                    theoryRepository,
                    solutionRepository,
                    timerRepository,
                    timer,
                    solutionIdGenerator,
                )

                override fun routes(): Router = Router.router(vertx).apply {
                    with(
                        HandlerDSL(
                            serializers,
                            authOptions.authenticationHandler,
                            authOptions.authorizationProvider,
                            BodyHandler.create()
                        )
                    ) {

                        post("/")
                            .produces(serializers.availableTypes)
                            .authenticationHandler()
                            .authorizationHandler(SolutionUseCases.Tags.createSolution)
                            .bodyHandler()
                            .dataHandler(HTTPStatusCode.CREATED) { ctx ->
                                val every = ctx.queryParam("every")
                                    .map(Duration::parse)
                                    .getOrNull(0)
                                val (name, data) = decodeJson(ctx.body, CreateSolutionDTO::class.java)
                                solutionUseCases.createSolution(name, data, every)
                            }

                        get("/:name")
                            .produces(serializers.availableTypes)
                            .authenticationHandler()
                            .authorizationHandler(SolutionUseCases.Tags.getSolution)
                            .dataHandler { ctx ->
                                val name = ctx.pathParam("name")
                                solutionUseCases.getSolution(solutionIdParser.parse(name))
                            }

                        get("/:name/history/:version")
                            .produces(serializers.availableTypes)
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
                            .authenticationHandler()
                            .authorizationHandler(SolutionUseCases.Tags.getResults)
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

                                val serializer = serializers.serializerForMimeType(mimeType)
                                    ?: error("Couldn't find serializer for $mimeType.")
                                val fws = ctx.request().toWebSocket()
                                fws
                                    .flatMap((::onSocketAccept)(name, skip, limit, within, serializer))
                                    .onFailure { fail ->
                                        fail.printStackTrace()
                                        ctx.fail(HTTPStatusCode.BAD_REQUEST.code)
                                    }
                            }

                        delete("/:name")
                            .authenticationHandler()
                            .authorizationHandler(SolutionUseCases.Tags.deleteByName)
                            .dataHandler(HTTPStatusCode.NO_CONTENT) { ctx ->
                                val name = ctx.pathParam("name")
                                solutionUseCases.deleteSolution(solutionIdParser.parse(name)).void()
                            }
                    }
                }

                fun onSocketAccept(
                    name: SolutionId,
                    skip: Int?,
                    limit: Int?,
                    within: Duration?,
                    serializer: BufferSerializer
                ): (ServerWebSocket) -> Future<Void> = { ws ->
                    vertx.executeBlocking<Iterator<Result>> { p ->
                        runBlocking {
                            val results = solutionUseCases.getResults(
                                name,
                                solverFactory,
                                GetResultsOptions(skip, limit, within)
                            ).iterator()
                            p.complete(results)
                        }
                    }.flatMap {
                        ws.onMessage("get") {
                            val next = if (it.hasNext()) it.next() else null
                            ws.write(serializer.serializeToBuffer(next))
                                .onSuccess { if (next == null) ws.close() }
                        }
                        ws.writeTextMessage("ready")
                    }
                }
            }
        }
    }
}
