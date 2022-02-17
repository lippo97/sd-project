package it.unibo.lpaas.authentication.provider

import com.fasterxml.jackson.core.type.TypeReference
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class InMemoryCredentialsProvider(private val memory: Map<Credentials, Role>) : CredentialsProvider {
    companion object {
        fun fromJsonFile(vertx: Vertx, inputStream: InputStream): Future<InMemoryCredentialsProvider> =
            vertx.executeBlocking<Map<Credentials, Role>> { p ->
                val values = String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                val users = DatabindCodec.mapper().readValue(values, object : TypeReference<List<UserDTO>>() {})
                val map = users.associate {
                    it.credentials to it.role
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
