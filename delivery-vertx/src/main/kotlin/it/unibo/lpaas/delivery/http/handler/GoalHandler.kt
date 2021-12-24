package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.auth.RBAC
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.delivery.StringParser
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.delivery.http.handler.dto.CreateGoalDTO
import it.unibo.lpaas.delivery.http.handler.dto.ReplaceGoalDTO
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal

@Suppress("LongMethod", "LongParameterList")
fun goalHandler(
    vertx: Vertx,
    goalRepository: GoalRepository,
    authenticationHandler: AuthenticationHandler,
    mimeMap: MimeMap<BufferSerializer>,
    goalIdParser: StringParser<GoalId>,
    rbac: RBAC,
): Router =
    Router.router(vertx).apply {
        val goalUseCases = GoalUseCases(goalRepository)

        get("/")
            .produces(mimeMap.availableTypes)
            .handler(authenticationHandler)
            .useCaseHandler(mimeMap, rbac) {
                goalUseCases.getAllGoalsIndex.map { list ->
                    list.map { "/goal/${it.show()}" }
                }
            }

        get("/:name")
            .produces(mimeMap.availableTypes)
            .handler(authenticationHandler)
            .useCaseHandler(mimeMap, rbac) { ctx ->
                val name = ctx.pathParam("name")
                goalUseCases.getGoalByName(goalIdParser(name))
            }

        post("/")
            .produces(mimeMap.availableTypes)
            .handler(authenticationHandler)
            .handler(BodyHandler.create())
            .useCaseHandler(mimeMap, rbac, HTTPStatusCode.CREATED) { ctx ->
                val (name, subgoals) = decodeJson(ctx.body, CreateGoalDTO::class.java)
                goalUseCases.createGoal(name, Goal.Data(subgoals))
            }

        put("/:name")
            .produces(mimeMap.availableTypes)
            .handler(authenticationHandler)
            .handler(BodyHandler.create())
            .useCaseHandler(mimeMap, rbac) { ctx ->
                val name = goalIdParser(ctx.pathParam("name"))
                val (subgoals) = decodeJson(ctx.body, ReplaceGoalDTO::class.java)
                goalUseCases.replaceGoal(name, Goal.Data(subgoals))
            }

        delete("/:name")
            .handler(authenticationHandler)
            .useCaseHandler(mimeMap, rbac) { ctx ->
                val name = goalIdParser(ctx.pathParam("name"))
                goalUseCases.deleteGoal(name).void()
            }

        patch("/:name")
            .produces(mimeMap.availableTypes)
            .handler(authenticationHandler)
            .handler(BodyHandler.create())
            .useCaseHandler(mimeMap, rbac, HTTPStatusCode.CREATED) { ctx ->
                val name = goalIdParser(ctx.pathParam("name"))
                val subgoal = decodeJson(ctx.body, Subgoal::class.java)
                goalUseCases.appendSubgoal(name, subgoal)
            }

        get("/:name/:index")
            .produces(mimeMap.availableTypes)
            .handler(authenticationHandler)
            .useCaseHandler(mimeMap, rbac) { ctx ->
                val name = goalIdParser(ctx.pathParam("name"))
                val index = ctx.pathParam("index").toInt()
                goalUseCases.getSubgoalByIndex(name, index)
            }

        put("/:name/:index")
            .produces(mimeMap.availableTypes)
            .handler(authenticationHandler)
            .handler(BodyHandler.create())
            .useCaseHandler(mimeMap, rbac) { ctx ->
                val name = goalIdParser(ctx.pathParam("name"))
                val index = ctx.pathParam("index").toInt()
                val subgoal = decodeJson(ctx.body, Subgoal::class.java)
                goalUseCases.replaceSubgoal(name, index, subgoal)
            }

        delete("/:name/:index")
            .handler(authenticationHandler)
            .useCaseHandler(mimeMap, rbac) { ctx ->
                val name = goalIdParser(ctx.pathParam("name"))
                val index = ctx.pathParam("index").toInt()
                goalUseCases.deleteSubgoal(name, index).void()
            }
    }
