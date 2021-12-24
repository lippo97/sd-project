import com.fasterxml.jackson.databind.module.SimpleModule
import io.kotest.assertions.shouldFail
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.databind.impl.StructDeserializer
import it.unibo.lpaas.domain.databind.impl.StructSerializer
import it.unibo.lpaas.domain.impl.IncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.MongoGoalRepository
import it.unibo.tuprolog.core.Struct
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration

object Mongo : Tag()

class MongoGoalRepositoryTest : FunSpec({
    tags(Mongo)

    val client = KMongo.createClient().coroutine

    KMongoConfiguration.registerBsonModule(
        SimpleModule().apply {
            addAbstractTypeMapping(Version::class.java, IncrementalVersion::class.java)
            addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
            addSerializer(Struct::class.java, StructSerializer())
            addDeserializer(Struct::class.java, StructDeserializer())
        }
    )

    test("The mongo service must be running") {
        shouldNotThrow<Exception> {
            client.getDatabase("goal-repo-test")
                .getCollection<Goal>()
        }
    }

    val database = client.getDatabase("goal-repo-test")
    val repository = MongoGoalRepository(database.getCollection())

    test("The database should be empty") {
        repository.findAll().isEmpty().shouldBeTrue()
    }

    context("When a new goal is submitted") {
        val exampleSubGoal = Subgoal(Struct.of("parent", Struct.of("goku"), Struct.of("gohan")))
        val exampleGoal = Goal(GoalId.of("exampleGoal"), Goal.Data(listOf(exampleSubGoal)))

        test("a document must be insert into the DB") {
            val createdGoal = repository.create(exampleGoal.name, exampleGoal.data)
            createdGoal.shouldBeEqualToComparingFields(exampleGoal)
        }

        test("the goals collection must be updated") {
            repository.findAll().size.shouldBeExactly(1)
            repository.findByName(exampleGoal.name).shouldBe(exampleGoal)
        }

        test("the goal should find by own name") {
            val exampleGoal2 = Goal(GoalId.of("exampleGoal2"), Goal.Data(listOf(exampleSubGoal)))
            repository.create(exampleGoal2.name, exampleGoal2.data)
            repository.findByName(exampleGoal.name).shouldBe(exampleGoal)
            repository.findByName(exampleGoal2.name).shouldBe(exampleGoal2)
        }

        xtest("the goal's id must be unique") {
            shouldFail {
                repository.create(exampleGoal.name, exampleGoal.data)
            }
        }
    }

    afterSpec {
        database.drop()
    }
})
