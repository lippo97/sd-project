package it.unibo.lpaas.auth.impl

import it.unibo.lpaas.auth.RBAC
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.Tag

class SimpleRBAC : RBAC {
    val permissions: Set<Pair<Role, Tag>> = setOf()

    override fun isAuthorized(role: Role, tag: Tag): Boolean {
        TODO("Not yet implemented")
    }

    override fun addPermission(role: Role, tag: Tag) {
        TODO("Not yet implemented")
    }

    override fun removePermission(role: Role, tag: Tag) {
        TODO("Not yet implemented")
    }
}
