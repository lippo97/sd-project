package it.unibo.lpaas

import io.vertx.core.Future
import io.vertx.core.Promise
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.delivery.http.auth.Token
import it.unibo.lpaas.delivery.http.auth.TokenStorage
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

data class TokenDTO(val token: String, val role: String)

class MongoTokenStorage(
    private val tokenCollection: CoroutineCollection<TokenDTO>
) : TokenStorage {
    override fun getRole(token: Token): Future<Role> {
        val promise = Promise.promise<Role>()
        GlobalScope.launch(Dispatchers.IO) {
            tokenCollection.findOne(TokenDTO::token eq token.value)?.run {
                promise.complete(Role.parse(role))
            } ?: promise.fail(UnauthorizedException())
        }
        return promise.future()
    }
}

fun TokenStorage.Companion.mongo(
    tokenCollection: CoroutineCollection<TokenDTO>,
): TokenStorage = MongoTokenStorage(tokenCollection)
