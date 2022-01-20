package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.core.TheoryUseCases
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.SerializerCollection
import it.unibo.lpaas.delivery.http.handler.dsl.HandlerDSL
import it.unibo.lpaas.delivery.http.handler.dto.CreateTheoryDTO
import it.unibo.lpaas.delivery.http.handler.dto.FactInTheoryDTO
import it.unibo.lpaas.delivery.http.handler.dto.ReplaceTheoryDTO
import it.unibo.lpaas.domain.Theory

interface TheoryController : Controller {

    companion object {

        @JvmStatic
        fun make(
            vertx: Vertx,
            theoryDependencies: TheoryDependencies,
            serializerCollection: SerializerCollection<BufferSerializer>,
            authOptions: Controller.AuthOptions,
        ): TheoryController = object : TheoryController {
            val theoryRepository = theoryDependencies.theoryRepository
            val theoryIdParser = theoryDependencies.theoryIdParser
            val functorParser = theoryDependencies.functorParser
            val incrementalVersionParser = theoryDependencies.incrementalVersionParser
            val theoryUseCase: TheoryUseCases = TheoryUseCases(theoryRepository)

            @Suppress("LongMethod")
            override fun routes(): Router = Router.router(vertx).apply {
                with(
                    HandlerDSL(
                        serializerCollection,
                        authOptions.authenticationHandler,
                        authOptions.authorizationProvider
                    )
                ) {
                    get("/")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.getAllTheoriesIndex)
                        .dataHandler {
                            theoryUseCase.getAllTheoriesIndex().map { "/theories/${it.show()}" }
                        }

                    post("/")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.createTheory)
                        .handler(BodyHandler.create())
                        .dataHandler(HTTPStatusCode.CREATED) { ctx ->
                            val (name, theory) = decodeJson(ctx.body, CreateTheoryDTO::class.java)
                            theoryUseCase.createTheory(name, Theory.Data(theory))
                        }

                    get("/:name")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.getTheoryByName)
                        .dataHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            theoryUseCase.getTheoryByName(name)
                        }

                    put("/:name")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.updateTheory)
                        .handler(BodyHandler.create())
                        .dataHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val (theory) = decodeJson(ctx.body, ReplaceTheoryDTO::class.java)
                            theoryUseCase.updateTheory(name, Theory.Data(theory))
                        }

                    delete("/:name")
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.deleteTheory)
                        .dataHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            theoryUseCase.deleteTheory(name).void()
                        }

                    post("/:name/facts")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.addFactToTheory)
                        .handler(BodyHandler.create())
                        .dataHandler(HTTPStatusCode.CREATED) { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val (fact) = decodeJson(ctx.body, FactInTheoryDTO::class.java)
                            val beginning = ctx
                                .queryParam("beginning")
                                .firstOrNull() != "false"
                            theoryUseCase.addFactToTheory(name, fact, beginning)
                        }

                    put("/:name/facts")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.updateFactInTheory)
                        .handler(BodyHandler.create())
                        .dataHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val (fact) = decodeJson(ctx.body, FactInTheoryDTO::class.java)
                            val beginning = ctx.queryParam("beginning")
                                .firstOrNull() != "false"
                            theoryUseCase.updateFactInTheory(name, fact, beginning)
                        }

                    get("/:name/facts/:functor")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.getFactsInTheory)
                        .dataHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val functor = functorParser.parse(ctx.pathParam("functor"))
                            theoryUseCase.getFactsInTheory(name, functor)
                        }

                    get("/:name/history/:versionOrTimestamp")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.getTheoryByVersion)
                        .dataHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val version = incrementalVersionParser.parse(ctx.pathParam("versionOrTimestamp"))
                            theoryUseCase.getTheoryByNameAndVersion(name, version)
                        }

                    delete("/:name/history/:versionOrTimestamp")
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.deleteTheoryByVersion)
                        .dataHandler { ctx ->
                            val name = theoryIdParser.parse(ctx.pathParam("name"))
                            val version = incrementalVersionParser.parse(ctx.pathParam("versionOrTimestamp"))
                            theoryUseCase.deleteTheoryByVersion(name, version).void()
                        }

                    get("/:name/history/:versionOrTimestamp/facts/:functor")
                        .produces(serializerCollection.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(TheoryUseCases.Tags.getFactsInTheoryByNameAndVersion)
                        .dataHandler { ctx ->
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
