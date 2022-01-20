package it.unibo.lpaas.auth

import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.core.Tag
import it.unibo.lpaas.core.TheoryUseCases

object Permissions {
    @JvmStatic
    @JvmName("defaultPermissions")
    fun default(): Map<Role, List<Tag>> = mapOf(
        Role.CONFIGURATOR to GoalUseCases.Tags.run {
            listOf(
                getAllGoals,
                getAllGoalsIndex,
                getGoalByName,
                createGoal,
                replaceGoal,
                deleteGoal,
                appendSubgoal,
                getSubgoalByIndex,
                replaceGoal,
                deleteSubgoal,
            )
        } + TheoryUseCases.Tags.run {
            listOf(
                getAllTheories,
                getAllTheoriesIndex,
                getTheoryByName,
                getTheoryByVersion,
                getFactsInTheory,
                getFactsInTheoryByNameAndVersion,
                createTheory,
                addFactToTheory,
                deleteTheory,
                updateTheory,
                updateFactInTheory,
                deleteTheoryByVersion,
            )
        },
        Role.CLIENT to GoalUseCases.Tags.run {
            listOf(
                getAllGoals,
                getAllGoalsIndex,
                getGoalByName,
            )
        } + TheoryUseCases.Tags.run {
            listOf(
                getAllTheories,
                getAllTheoriesIndex,
                getTheoryByName,
                getTheoryByVersion,
                getFactsInTheory,
                getFactsInTheoryByNameAndVersion,
            )
        },
        Role.SOURCE to listOf(TheoryUseCases.Tags.addFactToTheory, TheoryUseCases.Tags.updateFactInTheory)
    )
}
