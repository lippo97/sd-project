package it.unibo.lpaas.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
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


    coEvery { theoryRepository.findAll() } returns listOf()
    coEvery { theoryRepository.findByName(realId) } returns theory
    coEvery { theoryRepository.findByName(fakeId) } throws NotFoundException(fakeId, "Theory")
    coEvery { theoryRepository.create(realId, someData) } returns theory
    coEvery { theoryRepository.create(duplicateId, someData) } throws
        DuplicateIdentifierException(duplicateId, "Theory")
    coEvery { theoryRepository.create(realId, invalidData) } throws
        ValidationException()

    val theoryUseCases = TheoryUseCases(theoryRepository)

    context("getAllTheoriesIndex") {
        test("it should have the right tag") {
            theoryUseCases.getAllTheoriesIndex.tag shouldBe TheoryUseCases.Tags.getAllTheoriesIndex
        }

        test("it should return all the theories") {
            theoryUseCases.getAllTheoriesIndex.execute()

            coVerify { theoryRepository.findAll() }
        }
    }
    context("getTheoryByName") {
        test("it should have the right tag") {
            theoryUseCases.getTheoryByName(realId).tag shouldBe TheoryUseCases.Tags.getTheoryByName
        }

        test("it should return the matching theory") {
            theoryUseCases.getTheoryByName(realId).execute() shouldBe theory

            coVerify { theoryRepository.findByName(realId) }
        }

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
        test("it should return the created theory") {
            theoryUseCases.createTheory(realId, someData).execute() shouldBe theory
            coVerify { theoryRepository.create(realId, someData) }
        }
        test("it should throw duplicate identifier") {
            assertThrows<DuplicateIdentifierException> {
                theoryUseCases.createTheory(duplicateId, someData).execute()
            }
            coVerify { theoryRepository.create(duplicateId, someData) }
        }
        test("it should throw validation exception") {
            assertThrows<ValidationException> {
                theoryUseCases.createTheory(realId, invalidData).execute()
            }
            coVerify { theoryRepository.create(realId, invalidData) }
        }
    }
})
