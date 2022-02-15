package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.databind.impl.StructDeserializer
import it.unibo.lpaas.domain.databind.impl.StructSerializer
import it.unibo.lpaas.domain.databind.impl.TheoryDeserializer
import it.unibo.lpaas.domain.databind.impl.TheorySerializer
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.TimerRecord
import it.unibo.tuprolog.core.Struct
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration

object Mongo {
    private val mongoClientSettings = MongoClientSettings.builder().apply {
        applyConnectionString(ConnectionString(Environment.Mongo.CONNECTION_STRING))
    }.build()
    private val client = KMongo.createClient(mongoClientSettings).coroutine
    private val database = client.getDatabase(Environment.Mongo.APPLICATION_DATABASE)

    init {
        KMongoConfiguration.registerBsonModule(
            SimpleModule().apply {
                addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
                addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
                addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
                addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
                addSerializer(Struct::class.java, StructSerializer())
                addDeserializer(Struct::class.java, StructDeserializer())
                addSerializer(it.unibo.tuprolog.theory.Theory::class.java, TheorySerializer())
                addDeserializer(it.unibo.tuprolog.theory.Theory::class.java, TheoryDeserializer())
            }
        )
        println("Initialize mongo")
    }

    val timerCollection: CoroutineCollection<TimerRecord<Long>> by lazy { database.getCollection("timer") }

    val theoryRepository: CoroutineCollection<Theory> by lazy { database.getCollection("theory") }

    val goalRepository: CoroutineCollection<Goal> by lazy { database.getCollection("goal") }

    val solutionRepository: CoroutineCollection<Solution> by lazy { database.getCollection("solution") }
}
