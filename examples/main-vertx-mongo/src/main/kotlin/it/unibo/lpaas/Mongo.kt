package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.authentication.provider.Credentials
import it.unibo.lpaas.authentication.provider.UserDTO
import it.unibo.lpaas.authentication.serialization.RoleDeserializer
import it.unibo.lpaas.authentication.serialization.RoleSerializer
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
import it.unibo.lpaas.environment.Environment
import it.unibo.lpaas.persistence.TimerRecord
import it.unibo.tuprolog.core.Struct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.div
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import it.unibo.tuprolog.theory.Theory as Theory2P

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
                addSerializer(Theory2P::class.java, TheorySerializer())
                addDeserializer(Theory2P::class.java, TheoryDeserializer())
                addSerializer(Role::class.java, RoleSerializer())
                addDeserializer(Role::class.java, RoleDeserializer())
            }
        )
        println("Initialize mongo")
    }

    val timerCollection: CoroutineCollection<TimerRecord<Long>> by lazy { database.getCollection("timer") }

    val theoryRepository: CoroutineCollection<Theory> by lazy {
        database.getCollection<Theory>("theory").apply {
            GlobalScope.launch(Dispatchers.IO) {
                createIndex(Indexes.ascending(Theory::name.name, Theory::version.name), IndexOptions().unique(true))
            }
        }
    }

    val goalRepository: CoroutineCollection<Goal> by lazy { database.getCollection("goal") }

    val solutionRepository: CoroutineCollection<Solution> by lazy { database.getCollection("solution") }

    val userCollection: CoroutineCollection<UserDTO> by lazy {
        database.getCollection<UserDTO>("user").apply {
            GlobalScope.launch(Dispatchers.IO) {
                createIndex(
                    Indexes
                        .ascending((UserDTO::credentials / Credentials::username).name),
                    IndexOptions()
                        .unique(true)
                )
            }
        }
    }
}
