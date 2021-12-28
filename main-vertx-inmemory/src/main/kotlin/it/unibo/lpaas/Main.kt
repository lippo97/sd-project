package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec.mapper
import io.vertx.core.json.jackson.DatabindCodec.prettyMapper
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.GoalDependencies
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerFactory
import it.unibo.lpaas.delivery.http.bindAPIVersion
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.databind.DomainSerializationModule
import it.unibo.lpaas.domain.databind.configureMappers
import it.unibo.lpaas.domain.impl.IncrementalVersionImpl
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.ext.inMemory

@Suppress("MagicNumber", "SpreadOperator")
fun main() {

    val mimeMap = MimeMap.default()

    configureMappers(
        mapper(),
        prettyMapper(),
        *mimeMap.availableSerializers.map { it.objectMapper }.toTypedArray(),
    ) {
        registerKotlinModule()
        registerModule(DomainSerializationModule())
        registerModule(
            SimpleModule().apply {
                addAbstractTypeMapping(Version::class.java, IncrementalVersionImpl::class.java)
                addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
            }
        )
    }

    val vertx = Vertx.vertx()
    val controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            mimeMap = mimeMap,
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
                theoryIdParser = TheoryId::of
            ),
        )
    )

    vertx.createHttpServer()
        .bindAPIVersion(1, controller, vertx)
        .listen(8080).onComplete {
            println("Running...")
        }
}
