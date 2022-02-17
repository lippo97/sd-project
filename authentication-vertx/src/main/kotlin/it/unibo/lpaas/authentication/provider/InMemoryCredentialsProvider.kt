package it.unibo.lpaas.authentication.provider

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class InMemoryCredentialsProvider(private val memory: Map<Credentials, Role>) : CredentialsProvider {
    companion object {
        fun fromJsonFile(vertx: Vertx, inputStream: InputStream): Future<InMemoryCredentialsProvider> =
            vertx.executeBlocking<Map<Credentials, Role>> { p ->
                val values = JsonArray(String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
                val map = values
                    .map { it as JsonObject }
                    .associate {
                        val credentials = Credentials.fromJson(it.getJsonObject("credentials"))
                        val role = Role.parse(it.getString("role"))
                        credentials to role
                    }
                p.complete(map)
            }.map {
                InMemoryCredentialsProvider(it)
            }
    }

    override fun login(username: Username, password: Password): Future<Role> =
        memory[Credentials(username, password)]?.let {
            Future.succeededFuture(it)
        } ?: Future.failedFuture(UnauthorizedException())
}
