package it.unibo.lpaas.authentication.dto

import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.authentication.domain.Credentials

data class UserDTO(val credentials: Credentials, val role: Role)
