package it.unibo.lpaas

import io.vertx.core.Vertx
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.GoalDependencies
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerFactory
import it.unibo.lpaas.delivery.http.bindAPIVersion
import it.unibo.lpaas.delivery.http.databind.SerializerCollection
import it.unibo.lpaas.delivery.http.databind.SerializerConfiguration
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.ext.inMemory

@Suppress("MagicNumber", "SpreadOperator")
fun main() {
    val serializerCollection = SerializerCollection.default()

    SerializerConfiguration.defaultWithModule {
        addAbstractTypeMapping(Version::class.java, IntegerIncrementalVersion::class.java)
        addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
    }
        .applyOnJacksonAndSerializers(serializerCollection)

    val vertx = Vertx.vertx()
    val controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            serializerCollection = serializerCollection,
            authOptions = Controller.AuthOptions(
                authenticationHandler = AuthenticationHandlerFactory.alwaysGrant(),
                authorizationProvider = AuthorizationProvider.alwaysGrant(),
            ),
            goalDependencies = GoalDependencies(
                goalRepository = GoalRepository.inMemory(),
                goalIdParser = GoalId::of,
            ),
            theoryDependencies = TheoryDependencies(
                theoryRepository = TheoryRepository.inMemory { IncrementalVersion.zero },
                theoryIdParser = TheoryId::of,
                functorParser = { Functor(it) },
                incrementalVersionParser = { IncrementalVersion.of(Integer.parseInt(it))!! },
            ),
        )
    )

    vertx.createHttpServer()
        .bindAPIVersion(1, controller, vertx)
        .listen(8080).onComplete {
            println("Running...")
        }
}
