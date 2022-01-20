package it.unibo.lpaas.delivery.http.auth

import io.vertx.core.Future
import it.unibo.lpaas.auth.Role

interface TokenStorage {
    fun getRole(token: Token): Future<Role>

    companion object {
        fun inMemory(memory: Map<Token, Role>): TokenStorage = InMemoryTokenStorage(memory)

        fun inMemory(vararg pairs: Pair<Token, Role>): TokenStorage = inMemory(pairs.toMap())
    }
}
