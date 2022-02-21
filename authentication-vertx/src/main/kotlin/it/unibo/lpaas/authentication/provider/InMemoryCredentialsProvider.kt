package it.unibo.lpaas.authentication.provider

import com.fasterxml.jackson.core.type.TypeReference
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.authentication.bcrypt.BCrypt
import it.unibo.lpaas.authentication.domain.SecureCredentials
import it.unibo.lpaas.authentication.domain.Username
import it.unibo.lpaas.authentication.dto.SecureUserDTO
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import java.io.InputStream

class InMemoryCredentialsProvider(
    bCrypt: BCrypt,
    private val memory: Map<SecureCredentials, Role>,
) : HashedCredentialsProvider(bCrypt) {
    companion object {
        fun fromJsonFile(vertx: Vertx, bCrypt: BCrypt, inputStream: InputStream): Future<InMemoryCredentialsProvider> =
            vertx.executeBlocking<Map<SecureCredentials, Role>> { p ->
                val users = DatabindCodec.mapper()
                    .readValue(inputStream, object : TypeReference<List<SecureUserDTO>>() {})
                val map = users.associate {
                    it.credentials to it.role
                }
                p.complete(map)
            }.map {
                InMemoryCredentialsProvider(bCrypt, it)
            }
    }

    override fun credentialsForUser(username: Username): Future<SecureUserDTO> =
        memory.keys.firstOrNull { (u, _) -> u == username }.let {
            if (it != null) Future.succeededFuture(it)
            else Future.failedFuture(UnauthorizedException())
        }
            .map { SecureUserDTO(it, memory[it]!!) }
}
