package it.unibo.lpaas.domain

data class Goal(
    val name: GoalId,
    val data: Data,
) {
    data class Data(
        val subgoals: List<Subgoal>
    ) {
        fun append(subgoal: Subgoal): Data = copy(subgoals = subgoals + subgoal)

        fun replace(index: Int, subgoal: Subgoal): Data = copy(
            subgoals = subgoals.toMutableList().apply {
                this[index] = subgoal
            }
        )

        fun remove(index: Int): Data = copy(
            subgoals = subgoals.toMutableList().apply {
                removeAt(index)
            }
        )
    }
}
