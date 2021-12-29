package it.unibo.lpaas.delivery.http.handler.dto

import it.unibo.lpaas.domain.TheoryId
import it.unibo.tuprolog.theory.Theory as Theory2P

data class CreateTheoryDTO(val name: TheoryId, val value: Theory2P)

data class ReplaceTheoryDTO(val value: Theory2P)
