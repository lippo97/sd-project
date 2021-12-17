package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.Factories
import it.unibo.lpaas.delivery.http.MimeType
import it.unibo.lpaas.delivery.http.databind.BufferSerializer
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.databind.DomainSerializationModule
import it.unibo.lpaas.domain.databind.ObjectMappers
import it.unibo.lpaas.domain.databind.configureMappers
import it.unibo.lpaas.domain.impl.IncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.delete
import it.unibo.lpaas.http.get
import it.unibo.lpaas.http.patch
import it.unibo.lpaas.http.post
import it.unibo.lpaas.http.put
import it.unibo.lpaas.http.tap
import it.unibo.lpaas.persistence.InMemoryGoalRepository
import it.unibo.tuprolog.core.Struct
import kotlinx.coroutines.test.runTest

class HTTPGoalTest : FunSpec({

    val jsonMapper = ObjectMappers.json()
    val yamlMapper = ObjectMappers.yaml()

    configureMappers(DatabindCodec.mapper(), DatabindCodec.prettyMapper(), jsonMapper, yamlMapper) {
        registerKotlinModule()
        registerModule(DomainSerializationModule())
        registerModule(
            SimpleModule().apply {
                addAbstractTypeMapping(Version::class.java, IncrementalVersion::class.java)
                addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
            }
        )
    }

    val jsonSerializer = BufferSerializer.of(jsonMapper.writer())
    val yamlSerializer = BufferSerializer.of(yamlMapper.writer())

    val serializers = mapOf(
        MimeType.JSON to jsonSerializer,
        MimeType.YAML to yamlSerializer,
    )

    val vertx = Vertx.vertx()
    val client = vertx.createHttpClient()

    val exampleGoal = "parent(goku, gohan)"
    val anotherExampleGoal = "parent(vegeta, trunks)"

    beforeAny {
        runTest {
            val server = vertx.createHttpServer()
            val controller = Controller.make(
                DependencyGraph(
                    vertx = vertx,
                    serializers = serializers,
                    goalRepository = InMemoryGoalRepository(
                        mapOf(
                            StringId("default") to Goal.Data(listOf(Subgoal(Struct.of("parent"))))
                        )
                    ),
                    factories = Factories(
                        goalIdFactory = GoalId::of
                    )
                )
            )
            server
                .requestHandler(controller.routes())
                .listen(8080)
        }
    }

    test("The service should be running") {
        client.get("/goal")
            .map { it.statusCode() }
            .map { it shouldBeExactly 200 }
            .await()
    }

    test("The service should be initialized with a goal") {
        client.get("/goal")
            .flatMap { it.body() }
            .map { body ->
                body.toJsonArray().let {
                    it.size() shouldBeExactly 1
                    it.getString(0) shouldBe "/goal/default"
                }
            }
            .await()
    }

    context("When a new goal is submitted") {
        test("a resource must be created successfully") {
            client.post("/goal") {
                obj(
                    "name" to "myGoal",
                    "subgoals" to array(obj("value" to exampleGoal))
                )
            }
                .map { it.statusCode() }
                .map { it shouldBeExactly 201 }
                .await()
        }
        test("the goals index must be updated") {
            client.get("/goal")
                .flatMap { it.body() }
                .map {
                    it.toJson() shouldBe json {
                        array(
                            "/goal/default",
                            "/goal/myGoal",
                        )
                    }
                }
                .await()
        }
        test("it can be retrieved at its URI") {
            client.get("/goal/myGoal")
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().get<String>("name") shouldBe "myGoal"
                    it.toJsonObject()
                        .getJsonObject("data")
                        .getJsonArray("subgoals")
                        .getJsonObject(0)
                        .getString("value") shouldBe exampleGoal
                }
                .await()
        }
    }

    context("When an existing theory is replaced") {
        test("it should return the updated record") {
            client.put("/goal/default") {
                obj(
                    "subgoals" to array(obj("value" to anotherExampleGoal))
                )
            }
                .tap {
                    it.statusCode() shouldBeExactly 200
                }
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().getString("name") shouldBe "default"
                    it.toJsonObject()
                        .getJsonObject("data")
                        .getJsonArray("subgoals")
                        .getJsonObject(0)
                        .getString("value") shouldBe anotherExampleGoal
                }
                .await()
        }
    }

    context("When a goal is deleted") {
        test("request should complete successfully") {
            client.delete("/goal/myGoal/")
                .map {
                    it.statusCode() shouldBe 204
                }
                .await()
        }
        test("you shouldn't be able to retrieve it") {
            client.get("/goal/myGoal")
                .map { it.statusCode() }
                .map { it shouldBeExactly 404 }
                .await()
        }
    }

    context("When a subgoal is appended") {
        test("request should complete successfully") {
            client.patch("/goal/default") {
                obj(
                    "value" to "parent(bardok, goku)"
                )
            }
                .tap { it.statusCode() shouldBeExactly 201 }
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().getString("name") shouldBe "default"
                    it.toJsonObject()
                        .getJsonObject("data")
                        .getJsonArray("subgoals").apply {
                            size() shouldBeExactly 2
                            getJsonObject(1).getString("value") shouldBe "parent(bardok, goku)"
                        }
                }
                .await()
        }
        test("it can be retrieved at its index") {
            client.get("/goal/default/1")
                .tap { it.statusCode() shouldBeExactly 200 }
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().getString("value") shouldBe "parent(bardok, goku)"
                }
                .await()
        }
    }
    context("When a subgoal is replaced") {
        test("request should complete successfully") {
            client.put("/goal/default/1") {
                obj(
                    "value" to "parent(bulma, trunks)"
                )
            }
                .tap { it.statusCode() shouldBeExactly 200 }
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().getString("name") shouldBe "default"
                    it.toJsonObject()
                        .getJsonObject("data")
                        .getJsonArray("subgoals").apply {
                            size() shouldBeExactly 2
                            getJsonObject(1).getString("value") shouldBe "parent(bulma, trunks)"
                        }
                }
                .await()
        }
    }

    context("When a subgoal is deleted") {
        test("request should complete successfully") {
            client.delete("/goal/default/1")
                .map { it.statusCode() shouldBeExactly 204 }
                .await()
        }
        test("you shouldn't be able to retrieve the goal") {
            client.get("/goal/default/1")
                .map { it.statusCode() shouldBeExactly 404 }
                .await()
        }
    }

    context("When querying using the accept-content feature") {
        test("response should default to json if header was not provided") {
            client.get(
                "/goal/default",
            )
                .tap { it.statusCode() shouldBeExactly 200 }
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().getString("name") shouldBe "default"
                }
                .await()
        }
        test("response should be in the requested format (JSON)") {
            client.get(
                "/goal/default",
                headers = HttpHeaders.set(HttpHeaders.ACCEPT, MimeType.JSON.value)
            )
                .tap { it.statusCode() shouldBeExactly 200 }
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().getString("name") shouldBe "default"
                }
                .await()
        }
        test("response should be in the requested format (YAML)") {
            client.get(
                "/goal/default",
                headers = HttpHeaders.set(HttpHeaders.ACCEPT, MimeType.YAML.value)
            )
                .tap { it.statusCode() shouldBeExactly 200 }
                .flatMap { it.body() }
                .map {
                    it.toString() shouldBe """
                    ---
                    name: "default"
                    data:
                      subgoals:
                      - value: "parent(vegeta, trunks)"

                    """.trimIndent()
                }
                .await()
        }
    }
})
