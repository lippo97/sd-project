package it.unibo.lpaas.delivery.http.handler.dto

import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

data class CreateTheoryDTO(val name: TheoryId, val data: Theory.Data)

data class ReplaceTheoryDTO(val data: Theory.Data)

data class FactInTheoryDTO(val fact: Fact)
