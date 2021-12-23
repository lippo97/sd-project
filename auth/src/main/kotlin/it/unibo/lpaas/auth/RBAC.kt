package it.unibo.lpaas.auth

import it.unibo.lpaas.auth.impl.SimpleRBAC
import it.unibo.lpaas.core.Tag

interface RBAC {
    fun isAuthorized(role: Role, tag: Tag): Boolean
    fun addPermission(role: Role, tag: Tag)
    fun removePermission(role: Role, tag: Tag)
    fun addPermissions(role: Role, tags: List<Tag>)
    fun removePermissions(role: Role, tags: List<Tag>)

    companion object {
        fun default(): RBAC = SimpleRBAC().apply {
            addPermissions(Role.CLIENT, listOf("getAllGoals", "getAllGoalsIndex", "getGoalByName").map { Tag(it) })
            addPermissions(
                Role.CONFIGURATOR,
                listOf(
                    "getAllGoals", "getAllGoalsIndex", "getGoalByName", "createGoal", "replaceGoal", "deleteGoal",
                    "appendSubgoal", "getSubgoalByIndex", "replaceSubgoal", "deleteSubgoal"
                ).map { Tag(it) }
            )
        }
    }
}
