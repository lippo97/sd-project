package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.GoalDependencies
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.handler.dsl.HandlerDSL
import it.unibo.lpaas.delivery.http.handler.dto.CreateGoalDTO
import it.unibo.lpaas.delivery.http.handler.dto.ReplaceGoalDTO
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.http.databind.BufferSerializer
import it.unibo.lpaas.http.databind.SerializerCollection

interface GoalController : Controller {

    companion object {
        @JvmStatic
        fun make(
            vertx: Vertx,
            goalDependencies: GoalDependencies,
            authOptions: Controller.AuthOptions,
            serializers: SerializerCollection<BufferSerializer>,
        ): GoalController = object : GoalController {

            val goalRepository = goalDependencies.goalRepository
            val goalIdParser = goalDependencies.goalIdParser
            val goalUseCases = GoalUseCases(goalRepository)

            @Suppress("LongMethod")
            override fun routes(): Router = Router.router(vertx).apply {
                with(
                    HandlerDSL(
                        serializers,
                        authOptions.authenticationHandler,
                        authOptions.authorizationProvider,
                        BodyHandler.create(),
                    )
                ) {
                    get("/")
                        .produces(serializers.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.getAllGoalsIndex)
                        .dataHandler {
                            goalUseCases.getAllGoalsIndex()
                                .map { "/goal/${it.show()}" }
                        }

                    post("/")
                        .produces(serializers.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.createGoal)
                        .bodyHandler()
                        .dataHandler(HTTPStatusCode.CREATED) { ctx ->
                            val (name, data) = decodeJson(ctx.body, CreateGoalDTO::class.java)
                            goalUseCases.createGoal(name, data)
                        }

                    get("/:name")
                        .produces(serializers.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.getGoalByName)
                        .dataHandler { ctx ->
                            val name = ctx.pathParam("name")
                            goalUseCases.getGoalByName(goalIdParser.parse(name))
                        }

                    put("/:name")
                        .produces(serializers.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.replaceGoal)
                        .bodyHandler()
                        .dataHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val (data) = decodeJson(ctx.body, ReplaceGoalDTO::class.java)
                            goalUseCases.replaceGoal(name, data)
                        }

                    delete("/:name")
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.deleteGoal)
                        .dataHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            goalUseCases.deleteGoal(name).void()
                        }

                    patch("/:name")
                        .produces(serializers.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.appendSubgoal)
                        .bodyHandler()
                        .dataHandler(HTTPStatusCode.CREATED) { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val subgoal = decodeJson(ctx.body, Subgoal::class.java)
                            goalUseCases.appendSubgoal(name, subgoal)
                        }

                    get("/:name/:index")
                        .produces(serializers.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.getSubgoalByIndex)
                        .dataHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val index = ctx.pathParam("index").toInt()
                            goalUseCases.getSubgoalByIndex(name, index)
                        }

                    put("/:name/:index")
                        .produces(serializers.availableTypes)
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.replaceSubgoal)
                        .bodyHandler()
                        .dataHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val index = ctx.pathParam("index").toInt()
                            val subgoal = decodeJson(ctx.body, Subgoal::class.java)
                            goalUseCases.replaceSubgoal(name, index, subgoal)
                        }

                    delete("/:name/:index")
                        .authenticationHandler()
                        .authorizationHandler(GoalUseCases.Tags.deleteSubgoal)
                        .dataHandler { ctx ->
                            val name = goalIdParser.parse(ctx.pathParam("name"))
                            val index = ctx.pathParam("index").toInt()
                            goalUseCases.deleteSubgoal(name, index).void()
                        }
                }
            }
        }
    }
}
