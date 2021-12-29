package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.core.TheoryUseCases
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.delivery.http.handler.dsl.HandlerDSL
import it.unibo.lpaas.delivery.http.handler.dto.CreateTheoryDTO
import it.unibo.lpaas.delivery.http.handler.dto.ReplaceTheoryDTO
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Theory

interface TheoryController : Controller {

    companion object {

        @JvmStatic
        fun make(
            vertx: Vertx,
            theoryDependencies: TheoryDependencies,
            mimeMap: MimeMap<BufferSerializer>,
            authOptions: Controller.AuthOptions,
        ): TheoryController = object : TheoryController {
            val theoryRepository = theoryDependencies.theoryRepository
            val theoryIdParser = theoryDependencies.theoryIdParser
            val functorParser = theoryDependencies.functorParser
            val incrementalVersionParser = theoryDependencies.incrementalVersionParser
            val theoryUseCase: TheoryUseCases = TheoryUseCases(theoryRepository)

            @Suppress("LongMethod")
            override fun routes(): Router = Router.router(vertx).apply {
                with(HandlerDSL(mimeMap, authOptions.authenticationHandler, authOptions.authorizationProvider)) {
                    get("/")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler {
                            theoryUseCase.getAllTheoriesIndex.map { list ->
                                list.map { (id, version) -> "/theories/$id/version/$version" }
                            }
                        }

                    post("/")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .handler(BodyHandler.create())
                        .useCaseHandler(HTTPStatusCode.CREATED) { ctx ->
                            val (name, theory) = decodeJson(ctx.body, CreateTheoryDTO::class.java)
                            theoryUseCase.createTheory(name, Theory.Data(theory))
                        }

                    get("/:name")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            theoryUseCase.getTheoryByName(name)
                        }

                    put("/:name")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .handler(BodyHandler.create())
                        .useCaseHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val (theory) = decodeJson(ctx.body, ReplaceTheoryDTO::class.java)
                            theoryUseCase.updateTheory(name, Theory.Data(theory))
                        }

                    delete("/:name")
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            theoryUseCase.deleteTheory(name).void()
                        }

                    data class FactInTheoryDTO(val fact: Fact)
                    post("/:name/facts")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .handler(BodyHandler.create())
                        .useCaseHandler(HTTPStatusCode.CREATED) { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val (fact) = decodeJson(ctx.body, FactInTheoryDTO::class.java)
                            val beginning = ctx.queryParam("beginning")
                                .firstOrNull() != "false"
                            theoryUseCase.addFactToTheory(name, fact, beginning)
                        }

                    put("/:name/facts")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .handler(BodyHandler.create())
                        .useCaseHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val (fact) = decodeJson(ctx.body, FactInTheoryDTO::class.java)
                            val beginning = ctx.queryParam("beginning")
                                .firstOrNull() != "false"
                            theoryUseCase.updateFactInTheory(name, fact, beginning)
                        }

                    get("/:name/facts/:functor")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val functor = functorParser.parse(ctx.pathParam("functor"))
                            theoryUseCase.getFactsInTheory(name, functor)
                        }

                    get("/:name/history/:versionOrTimestamp")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val version = incrementalVersionParser.parse(ctx.pathParam("versionOrTimestamp"))
                            theoryUseCase.getTheoryByNameAndVersion(name, version)
                        }

                    delete("/:name/history/:versionOrTimestamp")
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val version = incrementalVersionParser.parse(ctx.pathParam("versionOrTimestamp"))
                            theoryUseCase.deleteTheoryByVersion(name, version).void()
                        }

                    get("/:name/history/:versionOrTimestamp/facts/:functor")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val version = incrementalVersionParser.parse(ctx.pathParam("versionOrTimestamp"))
                            val functor = functorParser.parse(ctx.pathParam("functor"))
                            theoryUseCase.getFactsInTheoryByNameAndVersion(name, functor, version)
                        }
                }
            }
        }
    }
}
