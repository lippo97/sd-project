package it.unibo.lpaas.authentication.bcrypt

import at.favre.lib.crypto.bcrypt.BCrypt.Result
import io.vertx.core.Future
import io.vertx.core.Vertx
import at.favre.lib.crypto.bcrypt.BCrypt as BCryptAlg

interface BCrypt {
    fun verify(password: String, hashed: String): Future<Boolean>

    fun hash(password: String): Future<String>

    fun hashSync(password: String): String

    companion object {

        fun vertx(vertx: Vertx, cost: Int = 6): BCrypt = object : BCrypt {
            override fun verify(password: String, hashed: String): Future<Boolean> =
                vertx.executeBlocking<Result> {
                    it.complete(BCryptAlg.verifyer().verify(password.toCharArray(), hashed))
                }
                    .map { it.verified }

            override fun hash(password: String): Future<String> =
                vertx.executeBlocking {
                    it.complete(hashSync(password))
                }

            override fun hashSync(password: String): String =
                BCryptAlg.withDefaults().hashToString(cost, password.toCharArray())
        }
    }
}
