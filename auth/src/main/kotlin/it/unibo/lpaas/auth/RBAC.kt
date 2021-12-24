package it.unibo.lpaas.auth

import it.unibo.lpaas.auth.impl.SimpleRBAC
import it.unibo.lpaas.core.Tag

interface RBAC {
    fun isAuthorized(role: Role, tag: Tag): Boolean

    fun authorizedRoles(tag: Tag): List<Role> = Role.values().filter { isAuthorized(it, tag) }

    companion object {

        fun configure(fn: ConfigurableRBAC.() -> Unit): RBAC = SimpleRBAC().apply(fn)

        fun simple(): RBAC = SimpleRBAC()

        // TODO: Questo diventerà il metodo factory di default
        fun default(): RBAC = configure {
            addPermissions(Role.CLIENT, listOf("getAllGoals", "getAllGoalsIndex", "getGoalByName").map { Tag(it) })
            addPermissions(
                Role.CONFIGURATOR,
                listOf(
                    "getAllGoals", "getAllGoalsIndex", "getGoalByName", "createGoal", "replaceGoal", "deleteGoal",
                    "appendSubgoal", "getSubgoalByIndex", "replaceSubgoal", "deleteSubgoal"
                ).map { Tag(it) }
            )
        }

        fun alwaysGrant(): RBAC = object : RBAC {
            override fun isAuthorized(role: Role, tag: Tag): Boolean = true
        }

        fun alwaysDeny(): RBAC = object : RBAC {
            override fun isAuthorized(role: Role, tag: Tag): Boolean = false
        }
    }
}
