package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec.mapper
import io.vertx.core.json.jackson.DatabindCodec.prettyMapper
import it.unibo.lpaas.auth.RBAC
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.Parsers
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerFactory
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.delivery.http.databind.MimeType
import it.unibo.lpaas.delivery.http.databind.ObjectMapperSerializer
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.databind.DomainSerializationModule
import it.unibo.lpaas.domain.databind.configureMappers
import it.unibo.lpaas.domain.impl.IncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.InMemoryGoalRepository

@Suppress("MagicNumber")
fun main() {

    val jsonSerializer = ObjectMapperSerializer.json()
    val yamlSerializer = ObjectMapperSerializer.yaml()
    val xmlSerializer = ObjectMapperSerializer.xml()

    configureMappers(
        mapper(),
        prettyMapper(),
        jsonSerializer.objectMapper,
        yamlSerializer.objectMapper,
        xmlSerializer.objectMapper,
    ) {
        registerKotlinModule()
        registerModule(DomainSerializationModule())
        registerModule(
            SimpleModule().apply {
                addAbstractTypeMapping(Version::class.java, IncrementalVersion::class.java)
                addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
            }
        )
    }

    val serializers = mapOf(
        MimeType.JSON to jsonSerializer,
        MimeType.YAML to yamlSerializer,
        MimeType.XML to xmlSerializer
    )

    val vertx = Vertx.vertx()
    val controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            mimeMap = MimeMap.of(serializers),
            authenticationHandler = AuthenticationHandlerFactory.alwaysGrant(),
            goalRepository = InMemoryGoalRepository(),
            parsers = Parsers(
                goalIdParser = GoalId::of
            ),
            rbac = RBAC.alwaysGrant(),
        )
    )

    vertx.createHttpServer()
        .requestHandler(controller.routes())
        .listen(8080).onComplete {
            println("Running...")
        }
}
