package it.unibo.lpaas.delivery.http.auth

import it.unibo.lpaas.auth.Role

interface TokenStorage {
    suspend fun getRole(token: String): Role
}
