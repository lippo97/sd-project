package it.unibo.lpaas.delivery.http.auth

import io.vertx.core.Future
import io.vertx.core.Vertx
import it.unibo.lpaas.auth.Role
import java.io.InputStream
import java.util.Properties
import kotlin.jvm.Throws

class InMemoryTokenStorage(private val memory: Map<Token, Role>) : TokenStorage {
    override fun getRole(token: Token): Future<Role> =
        memory[token]?.let {
            Future.succeededFuture(it)
        } ?: Future.failedFuture(UnauthorizedException())

    companion object {
        fun fromPropertyFile(vertx: Vertx, inputStream: InputStream): Future<InMemoryTokenStorage> =
            vertx.executeBlocking<Properties> {
                val properties = Properties().apply {
                    load(inputStream)
                }
                it.complete(properties)
            }.map {
                InMemoryTokenStorage(propertiesToMap(it))
            }

        @Throws(IllegalArgumentException::class)
        private fun propertiesToMap(properties: Properties): Map<Token, Role> = properties
            .map { (t, r) ->
                if (!(t is String && r is String)) {
                    throw IllegalArgumentException("t")
                }
                Token(t) to Role.parse(r)
            }
            .toMap()
    }
}
