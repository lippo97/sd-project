package it.unibo.lpaas.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
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
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import org.junit.jupiter.api.assertThrows

class SolutionUseCaseTest : FunSpec({
    val solutionRepository = mockk<SolutionRepository>()
    val goalRepository = mockk<GoalRepository>()
    val theoryRepository = mockk<TheoryRepository>()
    val solutionUseCases = SolutionUseCases(goalRepository, theoryRepository, solutionRepository)

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

        test("if the theoryId is not present it should throw") {
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

    context("find solution") {
        val solutionId = SolutionId.of("mySolution")
        val solutionData = mockk<Solution.Data>()
        val solution = Solution(solutionId, solutionData, IncrementalVersion.zero)
        test("it should return the specified solution") {
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
})
