package it.unibo.lpaas.auth.impl

import it.unibo.lpaas.auth.ConfigurableRBAC
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.Tag

class SimpleRBAC : ConfigurableRBAC {
    private var permissions: Set<Pair<Role, Tag>> = setOf()

    override fun isAuthorized(role: Role, tag: Tag) =
        permissions.contains(role to tag)

    override fun addPermission(role: Role, tag: Tag) {
        permissions += role to tag
    }

    override fun removePermission(role: Role, tag: Tag) {
        permissions -= role to tag
    }

    override fun addPermissions(role: Role, tags: List<Tag>) {
        tags.forEach { permissions += role to it }
    }

    override fun removePermissions(role: Role, tags: List<Tag>) {
        tags.forEach { permissions -= role to it }
    }
}
