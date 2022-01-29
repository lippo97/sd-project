package it.unibo.lpaas.domain

import java.time.Instant

data class Solution(
    val name: SolutionId,
    val data: Data,
    val version: IncrementalVersion,
    val createdAt: Instant = Instant.now(),
) {
    data class Data(
        val theoryOptions: TheoryOptions,
        val goalId: GoalId,
    )

    data class TheoryOptions(
        val name: TheoryId,
        val version: IncrementalVersion? = null
    )
}
