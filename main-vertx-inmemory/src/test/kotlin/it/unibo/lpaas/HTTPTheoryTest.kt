package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.collections.NonEmptyList
import it.unibo.lpaas.collections.nonEmptyListOf
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.GoalDependencies
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerTestFactory
import it.unibo.lpaas.delivery.http.bindAPIVersion
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.delivery.http.delete
import it.unibo.lpaas.delivery.http.get
import it.unibo.lpaas.delivery.http.post
import it.unibo.lpaas.delivery.http.put
import it.unibo.lpaas.delivery.http.tap
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.databind.DomainSerializationModule
import it.unibo.lpaas.domain.databind.configureMappers
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.ext.inMemory
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.theory.Theory as Theory2P

@Tags("HTTP")
class HTTPTheoryTest : FunSpec({
    val vertx = Vertx.vertx()
    val client = vertx.createHttpClient()

    val exampleTheory = """
    parent(bardok, goku).
    parent(goku, gohan).
    parent(gohan, pan).
    """.trimIndent()

    val theoryBaseUrl = Controller.API_VERSION + Controller.THEORY_BASEURL

    val initialMemory: Map<TheoryId, NonEmptyList<Theory>> = mapOf(
        StringId("default") to nonEmptyListOf(
            Theory(
                name = StringId("default"),
                data = Theory.Data(
                    Theory2P.of(
                        Clause.of(Struct.of("mario")),
                        Clause.of(Struct.of("luigi")),
                        Clause.of(Struct.of("peach")),
                    )
                ),
                version = IncrementalVersion.zero,
            )
        )
    )

    val mimeMap = MimeMap.default()

    configureMappers(
        DatabindCodec.mapper(),
        DatabindCodec.prettyMapper(),
        *mimeMap.availableSerializers.map { it.objectMapper }.toTypedArray(),
    ) {
        registerKotlinModule()
        registerModule(DomainSerializationModule())
        registerModule(JavaTimeModule())
        registerModule(
            SimpleModule().apply {
                addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
                addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
            }
        )
    }

    beforeAny {
        val server = vertx.createHttpServer()
        val controller = Controller.make(
            DependencyGraph(
                vertx = vertx,
                mimeMap = mimeMap,
                authOptions = Controller.AuthOptions(
                    authenticationHandler = AuthenticationHandlerTestFactory.alwaysGrantAndMockGroups(Role.CLIENT),
                    authorizationProvider = AuthorizationProvider.alwaysGrant(),
                ),
                goalDependencies = GoalDependencies(
                    goalRepository = GoalRepository.inMemory(),
                    goalIdParser = GoalId::of,
                ),
                theoryDependencies = TheoryDependencies(
                    theoryRepository = TheoryRepository.inMemory(initialMemory) { IncrementalVersion.zero },
                    theoryIdParser = TheoryId::of,
                    functorParser = { Functor(it) },
                    incrementalVersionParser = { IncrementalVersion.of(Integer.parseInt(it))!! },
                ),
            )
        )
        server
            .bindAPIVersion(1, controller, vertx)
            .listen(8081)
            .await()
    }

    test("The service should be running") {
        client.get(theoryBaseUrl, port = 8081)
            .map { it.statusCode() }
            .map { it shouldBeExactly 200 }
            .await()
    }

    test("The service should be initialized with a theory") {
        client.get(theoryBaseUrl, port = 8081)
            .flatMap { it.body() }
            .map {
                it.toJsonArray().apply {
                    size() shouldBeExactly 1
                    getString(0) shouldBe "/theories/default"
                }
            }
            .await()
    }

    context("When a new theory is submitted") {
        test("a resource must be created successfully") {
            client.post(theoryBaseUrl, port = 8081) {
                obj(
                    "name" to "myTheory",
                    "value" to exampleTheory,
                )
            }
                .map { it.statusCode() shouldBeExactly 201 }
                .await()
        }
        test("the body should be validated") {
            client.post(theoryBaseUrl, port = 8081) {
                obj(
                    "name" to "myTheory",
                    "value" to "someWrong(.",
                )
            }
                .map { it.statusCode() shouldBeExactly 400 }
                .await()
        }
        test("the theories index must be updated") {
            client.get(theoryBaseUrl, port = 8081)
                .flatMap { it.body() }
                .map {
                    it.toJson() shouldBe json {
                        array(
                            "/theories/default",
                            "/theories/myTheory"
                        )
                    }
                }
                .await()
        }

        test("it can be retrieved at its URI") {
            client.get("$theoryBaseUrl/myTheory", port = 8081)
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().apply {
                        getString("name") shouldBe "myTheory"
                        getJsonObject("version").getString("value") shouldBe "0"
                    }
                }
                .await()
        }
    }
    context("When an existing theory is replaced") {
        test("it should return the updated record") {
            client.put("$theoryBaseUrl/default", port = 8081) {
                obj(
                    "value" to """
                    another(valid, theory).
                    """.trimIndent()
                )
            }
                .tap { it.statusCode() shouldBeExactly 200 }
                .flatMap { it.body() }
                .map {
                    it.toJsonObject().apply {
                        getString("name") shouldBe "default"
                        getJsonObject("data")
                            .getString("value") shouldContain "another(valid, theory)"
                    }
                }
                .await()
        }
    }

    context("When a theory is deleted") {
        test("request should complete successfully") {
            client.delete("$theoryBaseUrl/myTheory", port = 8081)
                .map {
                    it.statusCode() shouldBeExactly 204
                }
                .await()
        }
        test("you shouldn't be able to retrieve it") {
            client.get("$theoryBaseUrl/myTheory")
                .map { it.statusCode() shouldBeExactly 404 }
        }
    }
    test("You shouldn't be able to delete a non-existing theory") {
        client.delete("$theoryBaseUrl/nonExisting", port = 8081)
            .map {
                it.statusCode() shouldBeExactly 404
            }
            .await()
    }
})
