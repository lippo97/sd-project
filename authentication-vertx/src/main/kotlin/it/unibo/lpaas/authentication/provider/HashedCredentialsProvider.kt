package it.unibo.lpaas.authentication.provider

import io.vertx.core.Future
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.authentication.bcrypt.BCrypt
import it.unibo.lpaas.authentication.domain.HashedPassword
import it.unibo.lpaas.authentication.domain.Password
import it.unibo.lpaas.authentication.domain.Username
import it.unibo.lpaas.authentication.dto.SecureUserDTO
import it.unibo.lpaas.delivery.http.exception.UnauthorizedException

abstract class HashedCredentialsProvider(private val bCrypt: BCrypt) : CredentialsProvider {
    abstract fun credentialsForUser(username: Username): Future<SecureUserDTO>

    override fun login(username: Username, password: Password): Future<Role> =
        credentialsForUser(username).flatMap { user ->
            bCrypt.verify(password, user.credentials.password).flatMap {
                if (it) {
                    Future.succeededFuture(user.role)
                } else {
                    Future.failedFuture(UnauthorizedException())
                }
            }
        }

    fun BCrypt.verify(password: Password, hashed: HashedPassword): Future<Boolean> =
        verify(password.value, hashed.value)
}
