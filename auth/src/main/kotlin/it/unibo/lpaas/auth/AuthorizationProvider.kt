package it.unibo.lpaas.auth

import it.unibo.lpaas.auth.impl.SimpleRBAC
import it.unibo.lpaas.core.Tag

interface AuthorizationProvider {
    fun isAuthorized(role: Role, tag: Tag): Boolean

    fun authorizedRoles(tag: Tag): List<Role> = Role.values().filter { isAuthorized(it, tag) }

    companion object {

        @JvmStatic
        fun configureRoleBased(fn: ConfigurableRBAC.() -> Unit): AuthorizationProvider = SimpleRBAC().apply(fn)

        @JvmStatic
        fun default(): AuthorizationProvider = configureRoleBased {
            addPermissions(Role.CLIENT, listOf("getAllGoals", "getAllGoalsIndex", "getGoalByName").map { Tag(it) })
            addPermissions(
                Role.CONFIGURATOR,
                listOf(
                    "getAllGoals", "getAllGoalsIndex", "getGoalByName", "createGoal", "replaceGoal", "deleteGoal",
                    "appendSubgoal", "getSubgoalByIndex", "replaceSubgoal", "deleteSubgoal"
                ).map { Tag(it) }
            )
        }

        @JvmStatic
        fun alwaysGrant(): AuthorizationProvider = object : AuthorizationProvider {
            override fun isAuthorized(role: Role, tag: Tag): Boolean = true
        }

        @JvmStatic
        fun alwaysDeny(): AuthorizationProvider = object : AuthorizationProvider {
            override fun isAuthorized(role: Role, tag: Tag): Boolean = false
        }
    }
}
