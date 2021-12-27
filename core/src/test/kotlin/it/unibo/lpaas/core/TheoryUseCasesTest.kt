package it.unibo.lpaas.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import org.junit.jupiter.api.assertThrows

internal class TheoryUseCasesTest : FunSpec({

    val realId = mockk<TheoryId>()
    val fakeId = mockk<TheoryId>()
    val duplicateId = mockk<TheoryId>()
    val theoryRepository = mockk<TheoryRepository>()
    val theory = mockk<Theory>()
    val someData = mockk<Theory.Data>()
    val invalidData = mockk<Theory.Data>()

    every { theory.name } returns realId

    afterAny {
        clearMocks(theoryRepository)
    }

    val theoryUseCases = TheoryUseCases(theoryRepository)

    context("getAllTheoriesIndex") {
        test("it should have the right tag") {
            theoryUseCases.getAllTheoriesIndex.tag shouldBe TheoryUseCases.Tags.getAllTheoriesIndex
        }

        coEvery { theoryRepository.findAll() } returns listOf(theory, theory, theory)
        test("it should return all the theories") {
            theoryUseCases.getAllTheoriesIndex.execute() shouldContainInOrder (listOf(theory, theory, theory).map { it.name })

            coVerify { theoryRepository.findAll() }
        }
    }
    context("getTheoryByName") {
        test("it should have the right tag") {
            theoryUseCases.getTheoryByName(realId).tag shouldBe TheoryUseCases.Tags.getTheoryByName
        }
        coEvery { theoryRepository.findByName(realId) } returns theory
        test("it should return the matching theory") {
            theoryUseCases.getTheoryByName(realId).execute() shouldBe theory

            coVerify { theoryRepository.findByName(realId) }
        }

        coEvery { theoryRepository.findByName(fakeId) } throws NotFoundException(fakeId, "Theory")
        test("it should throw not found") {
            assertThrows<NotFoundException> {
                theoryUseCases.getTheoryByName(fakeId).execute()
            }
            coVerify { theoryRepository.findByName(fakeId) }
        }
    }

    context("createTheory") {
        test("it should have the right tag") {
            theoryUseCases.createTheory(realId, someData).tag shouldBe TheoryUseCases.Tags.createTheory
        }
        coEvery { theoryRepository.create(realId, someData) } returns theory
        test("it should return the created theory") {
            theoryUseCases.createTheory(realId, someData).execute() shouldBe theory
            coVerify { theoryRepository.create(realId, someData) }
        }
        coEvery { theoryRepository.create(duplicateId, someData) } throws
            DuplicateIdentifierException(duplicateId, "Theory")
        test("it should throw duplicate identifier") {
            assertThrows<DuplicateIdentifierException> {
                theoryUseCases.createTheory(duplicateId, someData).execute()
            }
            coVerify { theoryRepository.create(duplicateId, someData) }
        }
        coEvery { theoryRepository.create(realId, invalidData) } throws
            ValidationException()
        test("it should throw validation exception") {
            assertThrows<ValidationException> {
                theoryUseCases.createTheory(realId, invalidData).execute()
            }
            coVerify { theoryRepository.create(realId, invalidData) }
        }
    }
})
