package it.unibo.lpaas

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.GoalDependencies
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.VertxHttpClient
import it.unibo.lpaas.delivery.http.auth.JWTAuthFactory
import it.unibo.lpaas.delivery.http.auth.Token
import it.unibo.lpaas.delivery.http.auth.TokenStorage
import it.unibo.lpaas.delivery.http.auth.inMemory
import it.unibo.lpaas.delivery.http.bindApi
import it.unibo.lpaas.delivery.http.databind.SerializerCollection
import it.unibo.lpaas.delivery.http.databind.SerializerConfiguration
import it.unibo.lpaas.delivery.http.handler.AuthController
import it.unibo.lpaas.delivery.http.tap
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.inMemory

@Tags("HTTP")
class AuthenticationE2E : FunSpec({

    val vertx = Vertx.vertx()
    val jwtProvider = JWTAuthFactory.hs256SecretBased(vertx, "keyboard cat")

    val port = 8083
    val client = VertxHttpClient.make(vertx, "localhost", port)
    val goalBaseUrl = Controller.API_VERSION + Controller.GOAL_BASEURL

    val serializerCollection = SerializerCollection.default()

    SerializerConfiguration.defaultWithModule {
        addAbstractTypeMapping(Version::class.java, IntegerIncrementalVersion::class.java)
        addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
    }
        .applyOnJacksonAndSerializers(serializerCollection)

    val tokenStorage = TokenStorage.inMemory(
        Token("goodToken") to Role.CONFIGURATOR
    )

    val controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            serializerCollection = serializerCollection,
            authOptions = Controller.AuthOptions(
                authenticationHandler = JWTAuthHandler.create(jwtProvider),
                authorizationProvider = AuthorizationProvider.configureRoleBased {
                    addPermission(Role.CONFIGURATOR, GoalUseCases.Tags.getAllGoalsIndex)
                },
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
        ),
    )

    beforeAny {
        vertx.createHttpServer()
            .requestHandler(
                Router.router(vertx).apply {
                    bindApi(1, controller)
                    mountSubRouter("/", AuthController.make(vertx, jwtProvider, tokenStorage).routes())
                }
            )
            .listen(port)
            .await()
    }

    context("When a user logged in") {
        lateinit var token: String
        test("it should return a valid token") {
            vertx.createHttpClient()
                .request(HttpMethod.POST, port, "localhost", "/login")
                .flatMap { it.send("goodToken") }
                .tap { it.statusCode() shouldBe 200 }
                .flatMap { it.body() }
                .map { token = it.toString() }
                .await()
        }
        test("the configurator shouldn't be able to get a single goal") {
            client.get(
                "$goalBaseUrl/anyId",
                headers = MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .map { it.statusCode() shouldBe 403 }
                .await()
        }
        test("the configurator should be able to retrieve all the goals") {
            client.get(
                goalBaseUrl,
                headers = MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .map { it.statusCode() shouldBe 200 }
                .await()
        }
        context("it should return unauthorized") {
            val invalidToken = "ieyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJncm91cHMiOlsiY2" +
                "xpZW50Il0sImlhdCI6MTY0MjY4OTE2NX0.JbTUgoUBQZnsTQcZigPWRa09NiKzQLJA1duqtHbn7k0"
            test("if the token is not valid") {
                client.get(
                    goalBaseUrl,
                    headers = MultiMap.caseInsensitiveMultiMap()
                        .add(HttpHeaders.AUTHORIZATION, "Bearer $invalidToken")
                )
                    .map { it.statusCode() shouldBe 401 }
                    .await()
            }
            test("if the token format is wrong") {
                client.get(
                    goalBaseUrl,
                    headers = MultiMap.caseInsensitiveMultiMap()
                        .add(HttpHeaders.AUTHORIZATION, "Bearer invalidToken")
                )
                    .map { it.statusCode() shouldBe 401 }
                    .await()
            }
        }
    }
})
