package it.unibo.lpaas.auth

import it.unibo.lpaas.core.Tag

interface RBAC {
    fun isAuthorized(role: Role, tag: Tag): Boolean
    fun addPermission(role: Role, tag: Tag)
    fun removePermission(role: Role, tag: Tag)
}
