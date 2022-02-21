package it.unibo.lpaas.authentication.provider

import io.vertx.core.Future
import io.vertx.core.Vertx
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.authentication.bcrypt.BCrypt
import it.unibo.lpaas.authentication.domain.Password
import it.unibo.lpaas.authentication.domain.SecureCredentials
import it.unibo.lpaas.authentication.domain.Username
import it.unibo.lpaas.authentication.dto.SecureUserDTO
import org.litote.kmongo.coroutine.CoroutineCollection

interface CredentialsProvider {
    fun login(username: Username, password: Password): Future<Role>

    companion object {

        fun inMemory(bCrypt: BCrypt, memory: Map<SecureCredentials, Role>): CredentialsProvider =
            InMemoryCredentialsProvider(bCrypt, memory)

        fun inMemory(bCrypt: BCrypt, vararg pairs: Pair<SecureCredentials, Role>): CredentialsProvider =
            inMemory(bCrypt, pairs.toMap())

        fun mongo(
            vertx: Vertx,
            bCrypt: BCrypt,
            collection: CoroutineCollection<SecureUserDTO>,
        ): CredentialsProvider = MongoCredentialsProvider(vertx, collection, bCrypt)
    }
}
