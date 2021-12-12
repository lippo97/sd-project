package it.unibo.lpaas.delivery.http.handler

import io.vertx.ext.web.Router
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.json.schema.SchemaParser
import io.vertx.kotlin.core.json.get
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.core.repository.GoalRepository
import it.unibo.lpaas.domain.GoalData
import it.unibo.lpaas.domain.Subgoal

fun goalHandler(router: Router, goalRepository: GoalRepository) {
    router.apply {
        get("/").coroutineHandler {
            GoalUseCases(goalRepository).getAllGoals()
        }

        post("/").coroutineHandler { ctx ->
            val name: String = ctx.bodyAsJson["name"]
            val subgoals: List<Subgoal> = ctx.bodyAsJson["subgoals"]
            val goalData = GoalData.of(subgoals)
            GoalUseCases(goalRepository).createGoal(name, goalData)
        }
    }
}
