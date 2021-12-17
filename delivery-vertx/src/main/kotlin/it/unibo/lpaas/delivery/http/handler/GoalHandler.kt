package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.delivery.http.HTTPStatusCode
import it.unibo.lpaas.delivery.http.MimeType
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.delivery.http.handler.dto.CreateGoalDTO
import it.unibo.lpaas.delivery.http.handler.dto.ReplaceGoalDTO
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal

fun goalHandler(
    vertx: Vertx,
    goalRepository: GoalRepository,
    serializers: Map<MimeType, BufferSerializer>,
    nameFactory: (String) -> GoalId,
): Router =
    Router.router(vertx).apply {
        val goalUseCases = GoalUseCases(goalRepository)

        get("/")
            .produces(serializers.keys)
            .jsonHandler(serializers) {
                goalUseCases.getAllGoalsIndex.map { list ->
                    list.map { "/goal/${it.show()}" }
                }
            }

        get("/:name")
            .produces(serializers.keys)
            .jsonHandler(serializers) { ctx ->
                val name = ctx.pathParam("name")
                goalUseCases.getGoalByName(nameFactory(name))
            }

        post("/")
            .handler(BodyHandler.create())
            .produces(serializers.keys)
            .jsonHandler(serializers, HTTPStatusCode.CREATED) { ctx ->
                val (name, subgoals) = decodeJson(ctx.body, CreateGoalDTO::class.java)
                goalUseCases.createGoal(name, Goal.Data(subgoals))
            }

        put("/:name")
            .handler(BodyHandler.create())
            .jsonHandler(serializers) { ctx ->
                val name = nameFactory(ctx.pathParam("name"))
                val (subgoals) = decodeJson(ctx.body, ReplaceGoalDTO::class.java)
                goalUseCases.replaceGoal(name, Goal.Data(subgoals))
            }

        delete("/:name")
            .jsonHandler(serializers) { ctx ->
                val name = nameFactory(ctx.pathParam("name"))
                goalUseCases.deleteGoal(name).void()
            }

        patch("/:name")
            .handler(BodyHandler.create())
            .produces(serializers.keys)
            .jsonHandler(serializers, HTTPStatusCode.CREATED) { ctx ->
                val name = nameFactory(ctx.pathParam("name"))
                val subgoal = decodeJson(ctx.body, Subgoal::class.java)
                goalUseCases.appendSubgoal(name, subgoal)
            }

        get("/:name/:index")
            .produces(serializers.keys)
            .jsonHandler(serializers) { ctx ->
                val name = nameFactory(ctx.pathParam("name"))
                val index = ctx.pathParam("index").toInt()
                goalUseCases.getSubgoalByIndex(name, index)
            }

        put("/:name/:index")
            .handler(BodyHandler.create())
            .produces(serializers.keys)
            .jsonHandler(serializers) { ctx ->
                val name = nameFactory(ctx.pathParam("name"))
                val index = ctx.pathParam("index").toInt()
                val subgoal = decodeJson(ctx.body, Subgoal::class.java)
                goalUseCases.replaceSubgoal(name, index, subgoal)
            }

        delete("/:name/:index")
            .produces(serializers.keys)
            .jsonHandler(serializers) { ctx ->
                val name = nameFactory(ctx.pathParam("name"))
                val index = ctx.pathParam("index").toInt()
                goalUseCases.deleteSubgoal(name, index)
            }
    }
