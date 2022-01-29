import com.fasterxml.jackson.databind.module.SimpleModule
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.unibo.lpaas.core.exception.DuplicateIdentifierException
import it.unibo.lpaas.core.exception.NotFoundException
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.MongoTimerRepository
import it.unibo.lpaas.persistence.TimerRecord
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import java.util.UUID

@Tags("Mongo")
class MongoTimerRepositoryTest : FunSpec({

    val client = KMongo.createClient().coroutine

    KMongoConfiguration.registerBsonModule(
        SimpleModule().apply {
            addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
        }
    )

    test("The mongo service must be running") {
        shouldNotThrow<Exception> {
            client.getDatabase("timer-repo-test")
                .getCollection<TimerRecord<UUID>>()
        }
    }

    val database = client.getDatabase("timer-repo-test")
    val collection = database.getCollection<TimerRecord<UUID>>()
    val repository = MongoTimerRepository(collection)

    test("The database should be empty") {
        collection.countDocuments() shouldBeExactly 0
    }

    context("When a timer is created") {
        val solutionId = SolutionId.of("mySolution")
        val timerID = UUID.randomUUID()
        test("it should return the timerID") {
            repository.create(solutionId, timerID) shouldBe timerID
        }
        xtest("it should throw on duplicate identifier") {
            shouldThrow<DuplicateIdentifierException> {
                repository.create(solutionId, timerID)
            }
        }
    }

    context("When a timer record is already present") {
        test("it should return the specified timerID") {
            val solutionId = SolutionId.of("mySolution")
            val timerID = repository.create(solutionId, UUID.randomUUID())
            repository.findByName(solutionId) shouldBe timerID
            val solutionId2 = SolutionId.of("mySolution2")
            val timerID2 = repository.create(solutionId2, UUID.randomUUID())
            repository.findByName(solutionId) shouldNotBe timerID2
            repository.findByName(solutionId2) shouldBe timerID2
        }
        test("it should throw on fake solutionId") {
            shouldThrow<NotFoundException> {
                repository.findByName(SolutionId.of("fakeSolutionId"))
            }
        }
    }

    context("When a timer record is deleted") {
        test("it should return the deleted timerID") {
            val solutionId = SolutionId.of("mySolution")
            val timerID = repository.create(solutionId, UUID.randomUUID())
            repository.deleteByName(solutionId) shouldBe timerID
            shouldThrow<NotFoundException> {
                repository.findByName(solutionId)
            }
        }
        test("it should throw on fake solutionId") {
            shouldThrow<NotFoundException> {
                repository.deleteByName(SolutionId.of("fakeSolutionId"))
            }
        }
        test("it should not throw on safe delete") {
            shouldNotThrow<NotFoundException> {
                repository.safeDeleteByName(SolutionId.of("fakeSolutionId"))
            }
        }
    }

    afterContainer {
        database.drop()
    }
})
