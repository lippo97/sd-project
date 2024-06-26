package it.unibo.lpaas.auth

import it.unibo.lpaas.core.Tag

interface ConfigurableRBAC : AuthorizationProvider {
    fun addPermission(role: Role, tag: Tag)
    fun removePermission(role: Role, tag: Tag)
    fun addPermissions(role: Role, tags: List<Tag>)
    fun removePermissions(role: Role, tags: List<Tag>)
}
