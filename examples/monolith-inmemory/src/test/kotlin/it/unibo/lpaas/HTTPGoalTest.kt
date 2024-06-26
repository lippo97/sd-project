package it.unibo.lpaas

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.core.timer.Timer
import it.unibo.lpaas.core.timer.TimerRepository
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.GoalDependencies
import it.unibo.lpaas.delivery.http.SolutionDependencies
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.TimerDependencies
import it.unibo.lpaas.delivery.http.VertxHttpClient
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerTestFactory
import it.unibo.lpaas.delivery.http.bindAPIVersion
import it.unibo.lpaas.delivery.http.tap
import it.unibo.lpaas.delivery.timer.vertx
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.MimeType
import it.unibo.lpaas.http.databind.SerializerCollection
import it.unibo.lpaas.http.databind.SerializerConfiguration
import it.unibo.lpaas.persistence.inMemory
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.classic.ClassicSolverFactory
import kotlinx.coroutines.test.runTest

@Tags("HTTP")
class HTTPGoalTest : FunSpec({

    val serializerCollection = SerializerCollection.default()

    SerializerConfiguration.defaultWithModule {
        addAbstractTypeMapping(Version::class.java, IntegerIncrementalVersion::class.java)
        addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
    }
        .applyOnJacksonAndSerializers(serializerCollection)
    val vertx = Vertx.vertx()
    val timer = Timer.vertx(vertx)
    val port = 8082
    val client = VertxHttpClient.make(vertx, "localhost", port)

    val exampleGoal = "parent(goku, gohan)"
    val anotherExampleGoal = "parent(vegeta, trunks)"

    val goalBaseUrl = Controller.API_VERSION + Controller.GOAL_BASEURL

    beforeAny {
        runTest {
            val server = vertx.createHttpServer()
            val controller = Controller.make(
                DependencyGraph(
                    vertx = vertx,
                    timerDependencies = TimerDependencies(
                        timer = timer,
                        timerRepository = TimerRepository.inMemory(),
                    ),
                    serializerCollection = serializerCollection,
                    authOptions = Controller.AuthOptions(
                        authenticationHandler = AuthenticationHandlerTestFactory.alwaysGrantAndMockGroups(Role.CLIENT),
                        authorizationProvider = AuthorizationProvider.alwaysGrant(),
                    ),
                    goalDependencies = GoalDependencies(
                        goalRepository = GoalRepository.inMemory(
                            StringId("default") to Goal.Data(listOf(Subgoal(Struct.of("parent"))))
                        ),
                        goalIdParser = GoalId::of,
                    ),
                    theoryDependencies = TheoryDependencies(
                        theoryRepository = TheoryRepository.inMemory { IncrementalVersion.zero },
                        theoryIdParser = TheoryId::of,
                        functorParser = { Functor(it) },
                        incrementalVersionParser = { IncrementalVersion.of(Integer.parseInt(it))!! },
                    ),
                    solutionDependencies = SolutionDependencies(
                        solutionRepository = SolutionRepository.inMemory { IncrementalVersion.zero },
                        solutionIdParser = SolutionId::of,
                        solutionIdGenerator = StringId::uuid,
                        solverFactory = ClassicSolverFactory,
                    )
                )
            )
            server
                .bindAPIVersion(1, controller, vertx)
                .listen(port)
        }
    }

    test("The service should be running") {
        client.get(goalBaseUrl)
            .map { it.statusCode() }
            .map { it shouldBeExactly 200 }
            .await()
    }

    test("The service should be initialized with a goal") {
        client.get(goalBaseUrl)
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
            client.post(goalBaseUrl) {
                obj(
                    "name" to "myGoal",
                    "data" to obj(
                        "subgoals" to array(obj("value" to exampleGoal))
                    )
                )
            }
                .map { it.statusCode() }
                .map { it shouldBeExactly 201 }
                .await()
        }
        test("the body should be validated") {
            client.post(goalBaseUrl) {
                obj(
                    "name" to "myGoal",
                    "data" to obj(
                        "subgoals" to array(obj("value" to "someWrong("))
                    )
                )
            }
                .map { it.statusCode() }
                .map { it shouldBeExactly 400 }
                .await()
        }
        test("the goals index must be updated") {
            client.get(goalBaseUrl)
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
            client.get("$goalBaseUrl/myGoal")
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

    context("When an existing goal is replaced") {
        test("it should return the updated record") {
            client.put("$goalBaseUrl/default") {
                obj(
                    "data" to obj(
                        "subgoals" to array(obj("value" to anotherExampleGoal))
                    )
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
            client.delete("$goalBaseUrl/myGoal/")
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
            client.patch("$goalBaseUrl/default") {
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
            client.get("$goalBaseUrl/default/1")
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
            client.put("$goalBaseUrl/default/1") {
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
            client.delete("$goalBaseUrl/default/1")
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
                "$goalBaseUrl/default",
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
                "$goalBaseUrl/default",
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
                "$goalBaseUrl/default",
                headers = HttpHeaders.set(HttpHeaders.ACCEPT, MimeType.YAML.value)
                    .set(HttpHeaders.AUTHORIZATION, "Bearer")
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
