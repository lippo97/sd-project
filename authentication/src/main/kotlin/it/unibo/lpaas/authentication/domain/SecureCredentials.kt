package it.unibo.lpaas.authentication.domain

data class SecureCredentials(val username: Username, val password: HashedPassword)
