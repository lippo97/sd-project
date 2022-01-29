package it.unibo.lpaas.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.core.timer.Timer
import it.unibo.lpaas.core.timer.TimerRepository
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Variable
import it.unibo.tuprolog.solve.Solver
import org.junit.jupiter.api.assertThrows

class SolutionUseCaseTest : FunSpec({
    val solutionRepository = mockk<SolutionRepository>()
    val goalRepository = mockk<GoalRepository>()
    val theoryRepository = mockk<TheoryRepository>()
    val timer = mockk<Timer<String>>()
    val timerRepository = mockk<TimerRepository<String>>()
    val solutionUseCases = SolutionUseCases(
        goalRepository,
        theoryRepository,
        solutionRepository,
        timerRepository,
        timer,
        { SolutionId.of("0") }
    )

    afterContainer {
        clearMocks(solutionRepository)
        clearMocks(goalRepository)
        clearMocks(theoryRepository)
    }

    context("createSolution") {
        val solutionId = SolutionId.of("mySolution")
        val goalId = GoalId.of("myGoal")
        val theoryId = TheoryId.of("myTheory")
        val solutionData = Solution.Data(Solution.TheoryOptions(theoryId), goalId)

        test("it should return a valid solution") {
            coEvery { goalRepository.unsafeExists(goalId) } returns Unit
            coEvery { theoryRepository.unsafeExists(theoryId) } returns Unit
            coEvery { solutionRepository.create(solutionId, solutionData) } returns
                Solution(solutionId, solutionData, IncrementalVersion.zero)
            solutionUseCases.createSolution(solutionId, solutionData).apply {
                name shouldBe solutionId
                version shouldBe IncrementalVersion.zero
                data shouldBe solutionData
            }
        }

        test("if the goalId is not present it should throw") {
            coEvery { goalRepository.unsafeExists(goalId) } throws NotFoundException(goalId, "Goal")
            val failure = assertThrows<NotFoundException> {
                solutionUseCases.createSolution(solutionId, solutionData)
            }
            failure.message shouldContain "Goal"
        }

        test("if the theoryId is not present it should throw") {
            coEvery { goalRepository.unsafeExists(goalId) } returns Unit
            coEvery { theoryRepository.unsafeExists(theoryId) } throws NotFoundException(theoryId, "Theory")
            val failure = assertThrows<NotFoundException> {
                solutionUseCases.createSolution(solutionId, solutionData)
            }
            failure.message shouldContain "Theory"
        }

        test("if the solutionId is already present it should throw") {
            coEvery { goalRepository.unsafeExists(goalId) } returns Unit
            coEvery { theoryRepository.unsafeExists(theoryId) } returns Unit
            coEvery { solutionRepository.create(solutionId, solutionData) } throws
                DuplicateIdentifierException(solutionId, "Solution")
            val failure = assertThrows<DuplicateIdentifierException> {
                solutionUseCases.createSolution(solutionId, solutionData)
            }
            failure.message shouldContain "Solution"
        }
    }

    context("getSolution") {
        val solutionId = SolutionId.of("mySolution")
        val solutionData = mockk<Solution.Data>()
        val solution = Solution(solutionId, solutionData, IncrementalVersion.zero)
        test("it should return the requested solution") {
            coEvery { solutionRepository.findByName(solutionId) } returns solution
            solutionUseCases.getSolution(solutionId) shouldBe solution
        }
        test("it should throw NotFoundException") {
            coEvery { solutionRepository.findByName(solutionId) } throws NotFoundException(solutionId, "Solution")
            shouldThrow<NotFoundException> {
                solutionUseCases.getSolution(solutionId)
            }
        }
    }

    context("getSolutionByVersion") {
        val solutionId = SolutionId.of("mySolution")
        val solutionData = mockk<Solution.Data>()
        val versionOne = IncrementalVersion.zero.next()
        val solution = Solution(solutionId, solutionData, versionOne)
        coEvery { solutionRepository.findByNameAndVersion(solutionId, any()) } throws
            NotFoundException(Any(), "Solution")
        coEvery { solutionRepository.findByNameAndVersion(solutionId, versionOne) } returns solution
        test("it should return the requested solution") {
            solutionUseCases.getSolutionByVersion(solutionId, versionOne) shouldBe solution
        }
        test("it should throw not found") {
            shouldThrow<NotFoundException> {
                solutionUseCases.getSolutionByVersion(solutionId, IncrementalVersion.of(3)!!)
            }
        }
    }

    context("getResults") {
        val solutionId = SolutionId.of("mySolution")
        val goalId = GoalId.of("myGoal")
        val theoryId = TheoryId.of("myTheory")
        val solutionData = Solution.Data(
            theoryOptions = Solution.TheoryOptions(theoryId),
            goalId,
        )
        val solution = Solution(solutionId, solutionData, IncrementalVersion.zero)

        test("it should return all the moves learnt by Charmander") {
            coEvery { solutionRepository.findByName(solutionId) } returns solution
            coEvery { theoryRepository.findByName(theoryId) } returns Pokemon.theory
            coEvery { goalRepository.findByName(goalId) } returns Pokemon.Goals.eventuallyLearns("charmander")
            val results = solutionUseCases.getResults(solutionId, Solver.prolog)
            results
                .toList()
                .dropLast(1)
                .map { it as Result.Yes }
                .map {
                    Pair(
                        it.variables[Variable("Pokemon")]?.asAtom().toString(),
                        it.variables[Variable("Move")]?.asAtom().toString(),
                    )
                }
                .toList()
                .shouldContainExactly(
                    "charmander" to "scratch",
                    "charmander" to "growl",
                    "charmander" to "ember",
                    "charmeleon" to "slash",
                    "charizard" to "fly",
                    "charizard" to "flamethrower",
                )
        }

        test("it should return all the fully evolved pokemons") {
            coEvery { solutionRepository.findByName(solutionId) } returns solution
            coEvery { theoryRepository.findByName(theoryId) } returns Pokemon.theory
            coEvery { goalRepository.findByName(goalId) } returns Pokemon.Goals.intermediateLevelPokemon
            val results = solutionUseCases.getResults(solutionId, Solver.prolog)
            results
                .toList()
                .dropLast(1)
                .map { it as Result.Yes }
                .map { it.variables[Variable("Pokemon")]?.asAtom().toString() }
                .toList()
                .shouldContainInOrder(
                    "charmeleon",
                    "wartortle",
                    "ivysaur"
                )
        }
    }

    context("deleteByName") {
        val solutionId = SolutionId.of("mySolution")

        test("it should return the deleted solution") {
            val goalId = GoalId.of("myGoal")
            val theoryId = TheoryId.of("myTheory")
            val solutionData = Solution.Data(
                theoryOptions = Solution.TheoryOptions(theoryId),
                goalId,
            )
            val solution = Solution(solutionId, solutionData, IncrementalVersion.zero)
            val timerId = "myTimer"
            coEvery { solutionRepository.deleteByName(solutionId) } returns solution
            coEvery { timerRepository.safeDeleteByName(solutionId) } returns timerId
            coEvery { timer.clear(timerId) } returns Unit
            solutionUseCases.deleteSolution(solutionId) shouldBe solution
        }
        test("it should throw NotFoundException on invalid id") {
            coEvery { solutionRepository.deleteByName(solutionId) } throws NotFoundException(solutionId, "Solution")
            shouldThrow<NotFoundException> {
                solutionUseCases.deleteSolution(solutionId)
            }
        }
    }
})
