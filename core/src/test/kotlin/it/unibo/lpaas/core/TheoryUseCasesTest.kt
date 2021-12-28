package it.unibo.lpaas.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.core.exception.ValidationException
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.getFactsByFunctor
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import org.junit.jupiter.api.assertThrows
import it.unibo.tuprolog.theory.Theory as Theory2P

internal class TheoryUseCasesTest : FunSpec({

    val realId = mockk<TheoryId>()
    val fakeId = mockk<TheoryId>()
    val duplicateId = mockk<TheoryId>()
    val notFoundId = mockk<TheoryId>()
    val theoryRepository = mockk<TheoryRepository>()
    val theory = mockk<Theory>()
    val someData = mockk<Theory.Data>()
    val invalidData = mockk<Theory.Data>()
    val defaultVersion = mockk<Version>()

    afterContainer {
        clearMocks(theoryRepository)
        clearMocks(theory)
    }

    val theoryUseCases = TheoryUseCases(theoryRepository)

    context("getAllTheoriesIndex") {
        test("it should have the right tag") {
            theoryUseCases.getAllTheoriesIndex.tag shouldBe TheoryUseCases.Tags.getAllTheoriesIndex
        }

        every { theory.name } returns realId
        coEvery { theoryRepository.findAll() } returns listOf(theory, theory, theory)
        test("it should return all the theories") {
            theoryUseCases.getAllTheoriesIndex.execute() shouldContainInOrder
                (listOf(theory, theory, theory).map { it.name })

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

    context("updateTheory") {
        test("it should have the right tag") {
            theoryUseCases.updateTheory(realId, someData).tag shouldBe TheoryUseCases.Tags.updateTheory
        }

        coEvery { theoryRepository.updateByName(realId, someData) } returns theory
        test("it should return the updated theory") {
            theoryUseCases.updateTheory(realId, someData).execute() shouldBe theory
        }

        coEvery { theoryRepository.updateByName(notFoundId, someData) } throws
            NotFoundException(notFoundId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.updateTheory(notFoundId, someData).execute()
            }
        }

        coEvery { theoryRepository.updateByName(realId, someData) } throws
            ValidationException("Theory")
        test("it should throw validation exception") {
            assertThrows<ValidationException> {
                theoryUseCases.updateTheory(realId, someData).execute()
            }
        }
    }

    context("deleteTheory") {
        test("it should have the right tag") {
            theoryUseCases.deleteTheory(realId).tag shouldBe TheoryUseCases.Tags.deleteTheory
        }

        coEvery { theoryRepository.deleteAllVersionsByName(realId) } returns theory
        test("it should return the deleted theory") {
            theoryUseCases.deleteTheory(realId).execute() shouldBe theory
        }

        coEvery { theoryRepository.deleteAllVersionsByName(notFoundId) } throws
            NotFoundException(notFoundId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.deleteTheory(notFoundId).execute()
            }
        }
    }

    context("getFactsInTheory") {
        test("it should have the right tag") {
            theoryUseCases.getFactsInTheory(realId, Functor("someFunctor")).tag shouldBe
                TheoryUseCases.Tags.getFactsInTheory
        }

        mockkStatic("it.unibo.lpaas.domain.Theory2PExtensionsKt")
        val someFunctor = Functor("someFunctor")
        val theory2P = mockk<Theory2P>()
        coEvery { theory2P.getFactsByFunctor(someFunctor) } returns listOf(Fact.of(Functor("someFact")))
        coEvery { theory.data } returns Theory.Data(theory2P)
        coEvery { theoryRepository.findByName(realId) } returns theory
        test("it should return the facts of the theory") {
            theoryUseCases.getFactsInTheory(realId, someFunctor).execute() shouldContainInOrder
                listOf(Fact.of(Functor("someFact")))
        }

        coEvery { theoryRepository.findByName(notFoundId) } throws
            NotFoundException(notFoundId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.getFactsInTheory(notFoundId, someFunctor).execute()
            }
        }
    }

    context("addFactToTheory") {
        test("it should have the right tag") {
            theoryUseCases.addFactToTheory(realId, Fact.of(Functor("someFact"))).tag shouldBe
                TheoryUseCases.Tags.addFactToTheory
        }

        val theory2P = spyk(
            Theory2P.of(
                Clause.of(Struct.of("mario")),
                Clause.of(Struct.of("luigi")),
            )
        )

        context("it should return the updated theory") {
            val theory = Theory(realId, Theory.Data(theory2P), Version.incremental)
            val updatedTheory = mockk<Theory>()
            coEvery { theoryRepository.findByName(realId) } returns theory
            coEvery { theoryRepository.updateByName(realId, any()) } returns updatedTheory

            test("it should prepend on beginning = true") {
                theoryUseCases.addFactToTheory(realId, Fact.of(Functor("wario"))).execute() shouldBe updatedTheory
                verify { theory2P.assertA(Struct.of("wario")) }
            }

            test("it should append on beginning = false") {
                theoryUseCases.addFactToTheory(realId, Fact.of(Functor("wario")), beginning = false)
                    .execute() shouldBe updatedTheory
                verify { theory2P.assertA(Struct.of("wario")) }
            }
        }

        coEvery { theoryRepository.findByName(notFoundId) } throws
            NotFoundException(notFoundId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.addFactToTheory(notFoundId, Fact.of(Functor("wario"))).execute()
            }
        }
    }

    context("updateFactInTheory") {
        test("it should have the right tag") {
            theoryUseCases.updateFactInTheory(realId, Fact.of(Functor("someFact"))).tag shouldBe
                TheoryUseCases.Tags.updateFactInTheory
        }

        coEvery { theoryRepository.findByName(realId) } returns theory
        every { theory.data } returns someData
        val updatedData = mockk<Theory.Data>()
        val updatedData2 = mockk<Theory.Data>()
        every { someData.retract(Functor("temperature"), 1) } returns updatedData
        every { updatedData.assertA(Fact.of(Functor("temperature"), "30")) } returns updatedData2
        val updatedTheory = mockk<Theory>()
        coEvery { theoryRepository.updateByName(realId, updatedData2) } returns updatedTheory
        test("it should return the updated theory") {
            theoryUseCases.updateFactInTheory(realId, Fact.of(Functor("temperature"), "30")).execute() shouldBe
                updatedTheory

            coVerify { theoryRepository.updateByName(realId, updatedData2) }
            verify { someData.retract(Functor("temperature"), 1) }
        }
    }

    context("getTheoryByVersion") {
        test("it should have the right tag") {
            theoryUseCases.getTheoryByNameAndVersion(realId, defaultVersion).tag shouldBe
                TheoryUseCases.Tags.getTheoryByVersion
        }

        coEvery { theoryRepository.findByNameAndVersion(notFoundId, defaultVersion) } throws
            NotFoundException(notFoundId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.getTheoryByNameAndVersion(notFoundId, defaultVersion).execute()
            }
        }

        coEvery { theoryRepository.findByNameAndVersion(realId, defaultVersion) } returns theory
        test("it should return the matching theory") {
            theoryUseCases.getTheoryByNameAndVersion(realId, defaultVersion).execute() shouldBe theory
            coVerify { theoryRepository.findByNameAndVersion(realId, defaultVersion) }
        }
    }

    context("deleteTheoryByVersion") {
        test("it should have the right tag") {
            theoryUseCases.deleteTheoryByVersion(realId, defaultVersion).tag shouldBe
                TheoryUseCases.Tags.deleteTheoryByVersion
        }

        coEvery { theoryRepository.deleteByNameAndVersion(realId, defaultVersion) } returns theory
        test("it should return the deleted theory") {
            theoryUseCases.deleteTheoryByVersion(realId, defaultVersion).execute() shouldBe theory
        }

        coEvery { theoryRepository.deleteByNameAndVersion(notFoundId, defaultVersion) } throws
            NotFoundException(notFoundId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.deleteTheoryByVersion(notFoundId, defaultVersion).execute()
            }
        }
    }
})
