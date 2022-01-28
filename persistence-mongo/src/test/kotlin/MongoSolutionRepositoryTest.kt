import com.fasterxml.jackson.databind.module.SimpleModule
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.MongoSolutionRepository
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration

@Tags("Mongo")
class MongoSolutionRepositoryTest : FunSpec({

    val client = KMongo.createClient().coroutine
    val dbName = "solution-repo-test"

    KMongoConfiguration.registerBsonModule(
        SimpleModule().apply {
            addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
            addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
            addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
            addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
        }
    )

    test("The mongo service must be running") {
        shouldNotThrow<Exception> {
            client.getDatabase(dbName)
                .getCollection<Theory>()
        }
    }

    val database = client.getDatabase(dbName)
    val repository = MongoSolutionRepository(database.getCollection()) { IncrementalVersion.zero }

    val theory2p = it.unibo.tuprolog.theory.Theory.of(
        Clause.of(Struct.of("mario")),
        Clause.of(Struct.of("luigi")),
        Clause.of(Struct.of("daisy")),
        Clause.of(Struct.of("peach")),
    )

    val theoryId = TheoryId.of("exampleTheory")
    val exampleTheory = Theory(theoryId, Theory.Data(theory2p), IncrementalVersion.zero)

    val goalId = GoalId.of("exampleGoal")

    val solutionId = SolutionId.of("mySolution")
    val solutionData = Solution.Data(Solution.TheoryOptions(theoryId, exampleTheory.version), goalId)
    val exampleSolution = Solution(solutionId, solutionData, IncrementalVersion.zero)

    context("When a new solution is submitted") {
        test("a document must be insert into the DB") {
            val createdSolution = repository.create(exampleSolution.name, exampleSolution.data)
            createdSolution.data shouldBeEqualToComparingFields exampleSolution.data
            createdSolution.version shouldBeEqualToComparingFields exampleSolution.version
        }

        context("the solution should be found") {
            repository.create(exampleSolution.name, exampleSolution.data)
            test("by name") {
                repository.findByName(exampleSolution.name).run {
                    data shouldBeEqualToComparingFields exampleSolution.data
                    version shouldBeEqualToComparingFields exampleSolution.version
                }
            }
            test("by name and version") {
                repository.findByNameAndVersion(exampleSolution.name, exampleSolution.version).run {
                    data shouldBeEqualToComparingFields exampleSolution.data
                    version shouldBeEqualToComparingFields exampleSolution.version
                }
            }
        }
    }

    context("When a solution is retrieved") {
        repository.create(exampleSolution.name, exampleSolution.data)
        test("the solution should be returned") {
            repository.findByName(exampleSolution.name).data shouldBeEqualToComparingFields exampleSolution.data
            repository.findByNameAndVersion(exampleSolution.name, exampleSolution.version).run {
                data shouldBeEqualToComparingFields exampleSolution.data
                version shouldBeEqualToComparingFields exampleSolution.version
            }
        }

        test("should be thrown if name doesn't exist") {
            val fakeId = SolutionId.of("fakeId")
            shouldThrow<NotFoundException> {
                repository.findByName(fakeId)
            }
        }

        test("should be thrown if version doesn't match") {
            val fakeVersion = IncrementalVersion.unsafeMakeInteger(5)
            shouldThrow<NotFoundException> {
                repository.findByNameAndVersion(exampleSolution.name, fakeVersion)
            }
        }
    }

    context("When a solution is deleted by name") {
        val createdSolution = repository.create(exampleSolution.name, exampleSolution.data)
        test("must return the deleted solution") {
            val deletedSolution = repository.deleteByName(exampleSolution.name)
            deletedSolution.data shouldBeEqualToComparingFields createdSolution.data
        }

        test("should thrown if name doesn't exist") {
            val fakeId = SolutionId.of("fakeId")
            shouldThrow<NotFoundException> {
                repository.deleteByName(fakeId)
            }
        }
    }

    context("When a solution is updated") {
        repository.create(exampleSolution.name, exampleSolution.data)

        test("must return the updated solution") {
            repository.updateByName(exampleSolution.name, exampleSolution.data).run {
                version shouldBeGreaterThan exampleSolution.version
                name shouldBeEqualToComparingFields exampleSolution.name
                createdAt shouldBeGreaterThan exampleSolution.createdAt
                data shouldBeEqualToComparingFields exampleSolution.data
            }
        }

        test("should thrown if name doesn't exist") {
            val fakeId = SolutionId.of("fakeId")
            shouldThrow<NotFoundException> {
                repository.updateByName(fakeId, exampleSolution.data)
            }
        }
    }

    afterContainer {
        database.drop()
    }
})
