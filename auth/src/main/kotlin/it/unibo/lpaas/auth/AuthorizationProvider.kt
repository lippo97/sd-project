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
            Permissions.default().entries.forEach { (role, tags) ->
                addPermissions(role, tags)
            }
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
