import com.fasterxml.jackson.databind.module.SimpleModule
import io.kotest.assertions.shouldFail
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.ints.shouldBeExactly
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.databind.impl.TheoryDeserializer
import it.unibo.lpaas.domain.databind.impl.TheorySerializer
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.MongoTheoryRepository
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import it.unibo.tuprolog.theory.Theory as Theory2P

@Tags("Mongo")
class MongoTheoryRepositoryTest : FunSpec({

    val client = KMongo.createClient().coroutine

    KMongoConfiguration.registerBsonModule(
        SimpleModule().apply {
            addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
            addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
            addSerializer(Theory2P::class.java, TheorySerializer())
            addDeserializer(Theory2P::class.java, TheoryDeserializer())
        }
    )

    test("The mongo service must be running") {
        shouldNotThrow<Exception> {
            client.getDatabase("theory-repo-test")
                .getCollection<Theory>()
        }
    }

    val database = client.getDatabase("theory-repo-test")
    val repository = MongoTheoryRepository(database.getCollection())

    test("The database should be empty") {
        repository.findAll().isEmpty().shouldBeTrue()
    }

    val theory2p = it.unibo.tuprolog.theory.Theory.of(
        Clause.of(Struct.of("mario")),
        Clause.of(Struct.of("luigi")),
        Clause.of(Struct.of("daisy")),
        Clause.of(Struct.of("peach")),
    )

    context("When a new theory is submitted") {
        val exampleTheory = Theory(TheoryId.of("exampleTheory"), Theory.Data(theory2p), IncrementalVersion.zero)

        test("a document must be insert into the DB") {
            val createdTheory = repository.create(exampleTheory.name, exampleTheory.data)
            createdTheory.data shouldBeEqualToComparingFields exampleTheory.data
            createdTheory.version shouldBeEqualToComparingFields exampleTheory.version
        }

        test("the theory collection must be updated") {
            repository.findAll().size.shouldBeExactly(1)
        }

        context("the theory should be found") {
            val theoryId = TheoryId.of("exampleTheory2")
            repository.create(theoryId, exampleTheory.data)
            test("by name") {
                repository.findByName(theoryId).run {
                    data shouldBeEqualToComparingFields exampleTheory.data
                    version shouldBeEqualToComparingFields exampleTheory.version
                }
            }
            test("by name and version") {
                repository.findByNameAndVersion(theoryId, exampleTheory.version).run {
//                    data shouldBeEqualToComparingFields exampleTheory.data
//                    version shouldBeEqualToComparingFields exampleTheory.version
                }
            }
        }

        xtest("the theory's id must be unique") {
            shouldFail {
                repository.create(exampleTheory.name, exampleTheory.data)
            }
        }
    }

    context("When a theory is retrieved") {
        val exampleTheory = Theory(TheoryId.of("exampleTheory"), Theory.Data(theory2p), IncrementalVersion.zero)
        repository.create(exampleTheory.name, exampleTheory.data)
        test("the theory should be returned") {
            repository.findByName(exampleTheory.name).data shouldBeEqualToComparingFields exampleTheory.data
            repository.findByNameAndVersion(exampleTheory.name, exampleTheory.version).run {
                data shouldBeEqualToComparingFields exampleTheory.data
                version shouldBeEqualToComparingFields exampleTheory.version
            }
        }

        test("should be thrown if name doesn't exist") {
            val fakeId = TheoryId.of("fakeId")
            shouldThrow<NotFoundException> {
                repository.findByName(fakeId)
            }
        }

        test("should be thrown if version doesn't match") {
            val fakeVersion = IncrementalVersion.unsafeMakeInteger(5)
            shouldThrow<NotFoundException> {
                repository.findByNameAndVersion(exampleTheory.name, fakeVersion)
            }
        }
    }

    xcontext("When a theory is updated") {
        val exampleTheory = Theory(TheoryId.of("exampleTheory"), Theory.Data(theory2p), IncrementalVersion.zero)
        repository.create(exampleTheory.name, exampleTheory.data)
        test("must return the updated theory") {
        }
    }

    afterContainer {
        database.drop()
    }
})
