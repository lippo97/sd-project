@file:Suppress("Filename", "MatchingDeclarationName")
package it.unibo.lpaas.delivery.http.handler.dto

import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId

data class CreateSolutionDTO(
    val name: SolutionId? = null,
    val data: Solution.Data,
)
