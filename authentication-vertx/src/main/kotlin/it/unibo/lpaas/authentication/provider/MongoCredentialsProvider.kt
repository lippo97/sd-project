package it.unibo.lpaas.authentication.provider

import io.vertx.core.Future
import io.vertx.core.Vertx
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

data class UserDTO(val credentials: Credentials, val role: Role)

class MongoCredentialsProvider(
    private val vertx: Vertx,
    private val credentialsCollection: CoroutineCollection<UserDTO>
) : CredentialsProvider {
    override fun login(username: Username, password: Password): Future<Role> =
        vertx.executeBlocking {
            runBlocking {
                credentialsCollection.findOne(
                    and(
                        Credentials::username::name eq username.value,
                        Credentials::password::name eq password.value,
                    )
                )?.run {
                    it.complete(role)
                } ?: it.fail(UnauthorizedException())
            }
        }
}
