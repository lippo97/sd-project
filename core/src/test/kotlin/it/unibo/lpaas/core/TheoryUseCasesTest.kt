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
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.getFactsByFunctor
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import org.junit.jupiter.api.assertThrows
import it.unibo.tuprolog.theory.Theory as Theory2P

internal class TheoryUseCasesTest : FunSpec({

    val realId = mockk<TheoryId>()
    val fakeId = mockk<TheoryId>()
    val duplicateId = mockk<TheoryId>()
    val theoryRepository = mockk<TheoryRepository>()
    val theory = mockk<Theory>()
    val someData = mockk<Theory.Data>()
    val invalidData = mockk<Theory.Data>()
    val defaultVersion = mockk<IncrementalVersion>()
    val functor = Functor("default")

    afterContainer {
        clearMocks(theoryRepository)
        clearMocks(theory)
    }

    val theoryUseCases = TheoryUseCases(theoryRepository)

    context("getAllTheoriesIndex") {
        every { theory.name } returns realId
        every { theory.version } returns defaultVersion
        coEvery { theoryRepository.findAll() } returns listOf(theory, theory, theory)
        test("it should return all the theories") {
            theoryUseCases.getAllTheoriesIndex().shouldContainInOrder(
                realId,
                realId,
                realId,
            )
            coVerify { theoryRepository.findAll() }
        }
    }

    context("getTheoryByName") {

        coEvery { theoryRepository.findByName(realId) } returns theory
        test("it should return the matching theory") {
            theoryUseCases.getTheoryByName(realId) shouldBe theory
            coVerify { theoryRepository.findByName(realId) }
        }

        coEvery { theoryRepository.findByName(fakeId) } throws NotFoundException(fakeId, "Theory")
        test("it should throw not found") {
            assertThrows<NotFoundException> {
                theoryUseCases.getTheoryByName(fakeId)
            }
            coVerify { theoryRepository.findByName(fakeId) }
        }
    }

    context("createTheory") {
        coEvery { theoryRepository.create(realId, someData) } returns theory
        test("it should return the created theory") {
            theoryUseCases.createTheory(realId, someData) shouldBe theory
            coVerify { theoryRepository.create(realId, someData) }
        }

        coEvery { theoryRepository.create(duplicateId, someData) } throws
            DuplicateIdentifierException(duplicateId, "Theory")
        test("it should throw duplicate identifier") {
            assertThrows<DuplicateIdentifierException> {
                theoryUseCases.createTheory(duplicateId, someData)
            }
            coVerify { theoryRepository.create(duplicateId, someData) }
        }

        coEvery { theoryRepository.create(realId, invalidData) } throws
            ValidationException()
        test("it should throw validation exception") {
            assertThrows<ValidationException> {
                theoryUseCases.createTheory(realId, invalidData)
            }
            coVerify { theoryRepository.create(realId, invalidData) }
        }
    }

    context("updateTheory") {

        coEvery { theoryRepository.updateByName(realId, someData) } returns theory
        test("it should return the updated theory") {
            theoryUseCases.updateTheory(realId, someData) shouldBe theory
        }

        coEvery { theoryRepository.updateByName(fakeId, someData) } throws
            NotFoundException(fakeId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.updateTheory(fakeId, someData)
            }
        }

        coEvery { theoryRepository.updateByName(realId, someData) } throws
            ValidationException("Theory")
        test("it should throw validation exception") {
            assertThrows<ValidationException> {
                theoryUseCases.updateTheory(realId, someData)
            }
        }
    }

    context("deleteTheory") {
        coEvery { theoryRepository.deleteByName(realId) } returns theory
        test("it should return the deleted theory") {
            theoryUseCases.deleteTheory(realId) shouldBe theory
        }

        coEvery { theoryRepository.deleteByName(fakeId) } throws
            NotFoundException(fakeId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.deleteTheory(fakeId)
            }
        }
    }

    context("getFactsInTheory") {
        mockkStatic("it.unibo.lpaas.domain.Theory2PExtensionsKt")
        val theory2P = mockk<Theory2P>()
        coEvery { theory2P.getFactsByFunctor(functor) } returns listOf(Fact.of(functor))
        coEvery { theory.data } returns Theory.Data(theory2P)
        coEvery { theoryRepository.findByName(realId) } returns theory
        test("it should return the facts of the theory") {
            theoryUseCases.getFactsInTheory(realId, functor) shouldContainInOrder
                listOf(Fact.of(functor))
        }

        coEvery { theoryRepository.findByName(fakeId) } throws
            NotFoundException(fakeId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.getFactsInTheory(fakeId, functor)
            }
        }
    }

    context("addFactToTheory") {
        val theory2P = spyk(
            Theory2P.of(
                Clause.of(Struct.of("mario")),
                Clause.of(Struct.of("luigi")),
            )
        )

        context("it should return the updated theory") {
            val myTheory = Theory(realId, Theory.Data(theory2P), IncrementalVersion.zero)
            val updatedTheory = mockk<Theory>()
            coEvery { theoryRepository.findByName(realId) } returns myTheory
            coEvery { theoryRepository.updateByName(realId, any()) } returns updatedTheory

            test("it should prepend on beginning = true") {
                theoryUseCases.addFactToTheory(realId, Fact.of(Functor("wario"))) shouldBe updatedTheory
                verify { theory2P.assertA(Clause.of(Struct.of("wario"))) }
            }

            test("it should append on beginning = false") {
                theoryUseCases.addFactToTheory(realId, Fact.of(Functor("wario")), beginning = false)
                    .shouldBe(updatedTheory)
                verify { theory2P.assertZ(Clause.of(Struct.of("wario"))) }
            }
        }

        coEvery { theoryRepository.findByName(fakeId) } throws
            NotFoundException(fakeId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.addFactToTheory(fakeId, Fact.of(Functor("wario")))
            }
        }
    }

    context("updateFactInTheory") {
        coEvery { theoryRepository.findByName(realId) } returns theory
        every { theory.data } returns someData
        val updatedData = mockk<Theory.Data>()
        val updatedData2 = mockk<Theory.Data>()
        every { someData.retract(Functor("temperature"), 1) } returns updatedData
        every { updatedData.assertA(Fact.of(Functor("temperature"), "30")) } returns updatedData2
        val updatedTheory = mockk<Theory>()
        coEvery { theoryRepository.updateByName(realId, updatedData2) } returns updatedTheory
        test("it should return the updated theory") {
            theoryUseCases.updateFactInTheory(realId, Fact.of(Functor("temperature"), "30")) shouldBe
                updatedTheory

            coVerify { theoryRepository.updateByName(realId, updatedData2) }
            verify { someData.retract(Functor("temperature"), 1) }
        }
    }

    context("getTheoryByVersion") {
        coEvery { theoryRepository.findByNameAndVersion(fakeId, defaultVersion) } throws
            NotFoundException(fakeId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.getTheoryByNameAndVersion(fakeId, defaultVersion)
            }
        }

        coEvery { theoryRepository.findByNameAndVersion(realId, defaultVersion) } returns theory
        test("it should return the matching theory") {
            theoryUseCases.getTheoryByNameAndVersion(realId, defaultVersion) shouldBe theory
            coVerify { theoryRepository.findByNameAndVersion(realId, defaultVersion) }
        }
    }

    context("deleteTheoryByVersion") {
        coEvery { theoryRepository.deleteByNameAndVersion(realId, defaultVersion) } returns theory
        test("it should return the deleted theory") {
            theoryUseCases.deleteTheoryByVersion(realId, defaultVersion) shouldBe theory
        }

        coEvery { theoryRepository.deleteByNameAndVersion(fakeId, defaultVersion) } throws
            NotFoundException(fakeId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.deleteTheoryByVersion(fakeId, defaultVersion)
            }
        }
    }

    context("getFactsInTheoryByNameAndVersion") {
        mockkStatic("it.unibo.lpaas.domain.Theory2PExtensionsKt")
        val theory2P = mockk<Theory2P>()
        coEvery { theory2P.getFactsByFunctor(functor) } returns listOf(Fact.of(functor))
        coEvery { theory.data } returns Theory.Data(theory2P)
        coEvery { theoryRepository.findByNameAndVersion(realId, defaultVersion) } returns theory
        test("it should return the facts of the theory") {
            theoryUseCases.getFactsInTheoryByNameAndVersion(realId, functor, defaultVersion)
                .shouldContainInOrder(listOf(Fact.of(functor)))
            coVerify { theoryRepository.findByNameAndVersion(realId, defaultVersion) }
        }

        coEvery { theoryRepository.findByNameAndVersion(fakeId, defaultVersion) } throws
            NotFoundException(fakeId, "Theory")
        test("it should throw not found identifier") {
            assertThrows<NotFoundException> {
                theoryUseCases.getFactsInTheoryByNameAndVersion(fakeId, functor, defaultVersion)
            }
        }
    }
})
