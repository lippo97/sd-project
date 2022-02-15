package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.core.http.ServerWebSocket
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
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
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.http.databind.BufferSerializer
import it.unibo.lpaas.http.databind.MimeType
import it.unibo.lpaas.http.databind.SerializerCollection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
                            authOptions.authorizationProvider
                        )
                    ) {

                        post("/")
                            .produces(serializers.availableTypes)
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
                                    .onSuccess((::onSocketAccept)(name, skip, limit, within, serializer))
                                    .onFailure { fail ->
                                        fail.printStackTrace()
                                        ctx.fail(HTTPStatusCode.BAD_REQUEST.code)
                                    }
                            }

                        delete("/:name")
                            .produces(serializers.availableTypes)
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
                ): (ServerWebSocket) -> Unit = { ws ->
                    GlobalScope.launch(vertx.dispatcher()) {
                        val solutions = solutionUseCases.getResults(
                            name,
                            solverFactory,
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
                            } else {
                                ws.write(serializer.serializeToBuffer(next))
                                    .onSuccess { ws.close() }
                            }
                        }
                        ws.writeTextMessage("ready")
                    }
                }
            }
        }
    }
}
