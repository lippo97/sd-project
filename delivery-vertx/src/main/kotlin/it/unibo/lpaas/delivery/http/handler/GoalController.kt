package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.GoalDependencies
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.delivery.http.handler.dsl.HandlerDSL
import it.unibo.lpaas.delivery.http.handler.dto.CreateGoalDTO
import it.unibo.lpaas.delivery.http.handler.dto.ReplaceGoalDTO
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.Subgoal

interface GoalController : Controller {

    companion object {
        @JvmStatic
        fun make(
            vertx: Vertx,
            goalDependencies: GoalDependencies,
            authOptions: Controller.AuthOptions,
            mimeMap: MimeMap<BufferSerializer>,
        ): GoalController = object : GoalController {

            val goalRepository = goalDependencies.goalRepository
            val goalIdParser = goalDependencies.goalIdParser
            val goalUseCases: GoalUseCases = GoalUseCases(goalRepository)

            @Suppress("LongMethod")
            override fun routes(): Router = Router.router(vertx).apply {
                with(HandlerDSL(mimeMap, authOptions.authenticationHandler, authOptions.authorizationProvider)) {
                    get("/")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler {
                            goalUseCases.getAllGoalsIndex.map { list ->
                                list.map { "/goal/${it.show()}" }
                            }
                        }

                    get("/")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler {
                            goalUseCases.getAllGoalsIndex.map { list ->
                                list.map { "/goal/${it.show()}" }
                            }
                        }

                    get("/:name")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = ctx.pathParam("name")
                            goalUseCases.getGoalByName(goalIdParser.parse(name))
                        }

                    post("/")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .handler(BodyHandler.create())
                        .useCaseHandler(HTTPStatusCode.CREATED) { ctx ->
                            val (name, subgoals) = decodeJson(ctx.body, CreateGoalDTO::class.java)
                            goalUseCases.createGoal(name, Goal.Data(subgoals))
                        }

                    put("/:name")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .handler(BodyHandler.create())
                        .useCaseHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val (subgoals) = decodeJson(ctx.body, ReplaceGoalDTO::class.java)
                            goalUseCases.replaceGoal(name, Goal.Data(subgoals))
                        }

                    delete("/:name")
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            goalUseCases.deleteGoal(name).void()
                        }

                    patch("/:name")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .handler(BodyHandler.create())
                        .useCaseHandler(HTTPStatusCode.CREATED) { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val subgoal = decodeJson(ctx.body, Subgoal::class.java)
                            goalUseCases.appendSubgoal(name, subgoal)
                        }

                    get("/:name/:index")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val index = ctx.pathParam("index").toInt()
                            goalUseCases.getSubgoalByIndex(name, index)
                        }

                    put("/:name/:index")
                        .produces(mimeMap.availableTypes)
                        .authenticationHandler()
                        .handler(BodyHandler.create())
                        .useCaseHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val index = ctx.pathParam("index").toInt()
                            val subgoal = decodeJson(ctx.body, Subgoal::class.java)
                            goalUseCases.replaceSubgoal(name, index, subgoal)
                        }

                    delete("/:name/:index")
                        .authenticationHandler()
                        .useCaseHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val index = ctx.pathParam("index").toInt()
                            goalUseCases.deleteSubgoal(name, index).void()
                        }
                }
            }
        }
    }
}
