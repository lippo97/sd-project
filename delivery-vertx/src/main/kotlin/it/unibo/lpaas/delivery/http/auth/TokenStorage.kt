package it.unibo.lpaas.delivery.http.auth

import io.vertx.core.Future
import it.unibo.lpaas.auth.Role

interface TokenStorage {
    fun getRole(token: Token): Future<Role>

    companion object {
        fun mock(): TokenStorage = object : TokenStorage {
            override fun getRole(token: Token): Future<Role> =
                Future.succeededFuture(Role.CONFIGURATOR)
        }
    }
}
