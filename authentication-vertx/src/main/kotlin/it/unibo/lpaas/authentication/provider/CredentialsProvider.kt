package it.unibo.lpaas.authentication.provider

import io.vertx.core.Future
import io.vertx.core.Vertx
import it.unibo.lpaas.auth.Role
import org.litote.kmongo.coroutine.CoroutineCollection

data class Username(val value: String)

data class Password(val value: String)

data class Credentials(val username: Username, val password: Password)

data class UserDTO(val credentials: Credentials, val role: Role)

interface CredentialsProvider {
    fun login(username: Username, password: Password): Future<Role>

    companion object {

        fun inMemory(memory: Map<Credentials, Role>): CredentialsProvider = InMemoryCredentialsProvider(memory)

        fun inMemory(vararg pairs: Pair<Credentials, Role>): CredentialsProvider = inMemory(pairs.toMap())

        fun mongo(
            vertx: Vertx,
            collection: CoroutineCollection<UserDTO>
        ): CredentialsProvider = MongoCredentialsProvider(vertx, collection)
    }
}
