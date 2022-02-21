package it.unibo.lpaas.authentication.dto

import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.authentication.domain.SecureCredentials

data class SecureUserDTO(val credentials: SecureCredentials, val role: Role)
