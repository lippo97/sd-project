package it.unibo.lpaas.authentication.domain

import io.vertx.core.Future
import it.unibo.lpaas.authentication.bcrypt.BCrypt

data class Password(val value: String) {
    fun hash(bCrypt: BCrypt): Future<HashedPassword> = bCrypt.hash(value).map { HashedPassword(it) }
}
