package it.unibo.lpaas.authentication.provider

import io.vertx.core.Future
import io.vertx.core.Vertx
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.div
import org.litote.kmongo.eq

class MongoCredentialsProvider(
    private val vertx: Vertx,
    private val credentialsCollection: CoroutineCollection<UserDTO>
) : CredentialsProvider {
    override fun login(username: Username, password: Password): Future<Role> =
        vertx.executeBlocking { p ->
            runBlocking {
                kotlin.runCatching {
                    credentialsCollection.findOne(
                        and(
                            (UserDTO::credentials / Credentials::username / Username::value) eq username.value,
                            (UserDTO::credentials / Credentials::password / Password::value) eq password.value
                        )
                    )?.let { p.complete(it.role) } ?: p.fail(UnauthorizedException())
                }.getOrElse { p.fail(it) }
            }
        }
}
