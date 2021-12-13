package it.unibo.lpaas.delivery.http.handler

import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.get
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.domain.GoalData
import it.unibo.lpaas.domain.Subgoal

fun goalHandler(vertx: Vertx, goalRepository: GoalRepository): Router =
    Router.router(vertx).apply {
        get("/").coroutineHandler { ctx ->
            val res = GoalUseCases(goalRepository).getAllGoals.execute()
            ctx.json(res)
        }

        post("/")
            .handler(BodyHandler.create())
            .coroutineHandler { ctx ->
                runCatching {
                    val name: String = ctx.bodyAsJson["name"]
                    val subgoals: JsonObject = ctx.bodyAsJson["data"]
                    println(name)
                    println(subgoals)
                    ctx.response()
                        .end()
                }
                    .recover {
                        if (it is ClassCastException) throw ValidationException()
                        else throw it
                    }

//            val goalData = GoalData.of(subgoals)

//            val res = GoalUseCases(goalRepository).createGoal(name, goalData)
//            ctx.json(res)
        }
    }
