package it.unibo.lpaas.authentication.provider

import io.vertx.core.Future
import io.vertx.core.Vertx
import it.unibo.lpaas.authentication.bcrypt.BCrypt
import it.unibo.lpaas.authentication.domain.Credentials
import it.unibo.lpaas.authentication.domain.Username
import it.unibo.lpaas.authentication.dto.SecureUserDTO
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.div
import org.litote.kmongo.eq

class MongoCredentialsProvider(
    private val vertx: Vertx,
    private val credentialsCollection: CoroutineCollection<SecureUserDTO>,
    bCrypt: BCrypt,
) : HashedCredentialsProvider(bCrypt) {

    override fun credentialsForUser(username: Username): Future<SecureUserDTO> =
        vertx.executeBlocking { p ->
            runBlocking {
                runCatching {
                    credentialsCollection.findOne(
                        (SecureUserDTO::credentials / Credentials::username / Username::value) eq username.value,
                    )?.let { p.complete(it) } ?: p.fail(UnauthorizedException())
                }.onFailure(p::fail)
            }
        }
}
