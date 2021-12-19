package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec.mapper
import io.vertx.core.json.jackson.DatabindCodec.prettyMapper
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.Factories
import it.unibo.lpaas.delivery.http.MimeType
import it.unibo.lpaas.delivery.http.databind.ObjectMapperSerializer
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.databind.DomainSerializationModule
import it.unibo.lpaas.domain.databind.ObjectMappers
import it.unibo.lpaas.domain.databind.configureMappers
import it.unibo.lpaas.domain.impl.IncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.InMemoryGoalRepository

@Suppress("MagicNumber")
fun main() {

    val jsonSerializer = ObjectMapperSerializer.of(ObjectMappers.json())
    val yamlSerializer = ObjectMapperSerializer.of(ObjectMappers.yaml())

    configureMappers(mapper(), prettyMapper(), jsonSerializer.objectMapper, yamlSerializer.objectMapper) {
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
    )

    val vertx = Vertx.vertx()
    val controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            serializers = serializers,
            goalRepository = InMemoryGoalRepository(),
            factories = Factories(
                goalIdFactory = GoalId::of
            )
        )
    )

    vertx.createHttpServer()
        .requestHandler(controller.routes())
        .listen(8080).onComplete {
            println("Running...")
        }
}
