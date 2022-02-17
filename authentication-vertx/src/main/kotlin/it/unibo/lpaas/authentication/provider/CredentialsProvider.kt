package it.unibo.lpaas.authentication.provider

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import it.unibo.lpaas.auth.Role

@JvmInline
value class Username(val value: String)

@JvmInline
value class Password(val value: String)

data class Credentials(val username: Username, val password: Password) {
    companion object {
        fun fromJson(jsonObject: JsonObject) = jsonObject.runCatching {
            Credentials(Username(getString("username")), Password(getString("password")))
        }.getOrElse { throw IllegalArgumentException("Unable to parse credentials") }
    }
}

interface CredentialsProvider {
    fun login(username: Username, password: Password): Future<Role>

    companion object {

        fun inMemory(memory: Map<Credentials, Role>): CredentialsProvider = InMemoryCredentialsProvider(memory)

        fun inMemory(vararg pairs: Pair<Credentials, Role>): CredentialsProvider = inMemory(pairs.toMap())

        fun mongo(): CredentialsProvider = TODO()
    }
}
