package it.unibo.lpaas

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.GoalUseCases
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
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerFactory
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerTestFactory
import it.unibo.lpaas.delivery.http.bindAPIVersion
import it.unibo.lpaas.delivery.timer.vertx
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.SerializerCollection
import it.unibo.lpaas.http.databind.SerializerConfiguration
import it.unibo.lpaas.persistence.inMemory
import it.unibo.tuprolog.solve.classic.ClassicSolverFactory

@Tags("HTTP")
class HTTPAuthTest : FunSpec({

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
    val goalBaseUrl = Controller.API_VERSION + Controller.GOAL_BASEURL

    fun makeControllerOf(
        authenticationHandler: AuthenticationHandler,
        authorizationProvider: AuthorizationProvider
    ): Controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            timerDependencies = TimerDependencies(
                timer = timer,
                timerRepository = TimerRepository.inMemory(),
            ),
            serializerCollection = serializerCollection,
            authOptions = Controller.AuthOptions(
                authenticationHandler = authenticationHandler,
                authorizationProvider = authorizationProvider,
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
            solutionDependencies = SolutionDependencies(
                solutionRepository = SolutionRepository.inMemory { IncrementalVersion.zero },
                solutionIdParser = SolutionId::of,
                solutionIdGenerator = StringId::uuid,
                solverFactory = ClassicSolverFactory,
            )
        ),
    )

    suspend fun withHttpServerOf(controller: Controller, fn: suspend () -> Unit) {
        with(
            vertx.createHttpServer()
                .bindAPIVersion(1, controller, vertx)
        ) {
            listen(port).await()
            fn()
            close().await()
        }
    }

    context("When an authenticated user makes a request") {
        test("it should return the result") {
            withHttpServerOf(
                makeControllerOf(
                    authenticationHandler = AuthenticationHandlerTestFactory.alwaysGrantAndMockGroups(Role.CLIENT),
                    authorizationProvider = AuthorizationProvider.alwaysGrant()
                )
            ) {
                client.get(goalBaseUrl)
                    .map { it.statusCode() shouldBe 200 }
                    .await()
            }
        }
    }

    context("When a non-authenticated user makes a request") {
        test("it should return 401") {
            withHttpServerOf(
                makeControllerOf(
                    authenticationHandler = AuthenticationHandlerTestFactory.alwaysDeny(),
                    authorizationProvider = AuthorizationProvider.alwaysGrant()
                )
            ) {
                client.get(goalBaseUrl)
                    .map { it.statusCode() shouldBe 401 }
                    .await()
            }
        }
    }

    context("When an operation requires a role") {
        test("it should return 403 if the user has no roles") {
            withHttpServerOf(
                makeControllerOf(
                    authenticationHandler = AuthenticationHandlerFactory.alwaysGrant(),
                    authorizationProvider = AuthorizationProvider.configureRoleBased {
                        addPermission(Role.CONFIGURATOR, GoalUseCases.Tags.getAllGoals)
                    }
                )
            ) {
                client.get(goalBaseUrl)
                    .map { it.statusCode() shouldBe 403 }
                    .await()
            }
        }
        test("it should return 403 if the user doesn't have it") {
            withHttpServerOf(
                makeControllerOf(
                    authenticationHandler = AuthenticationHandlerTestFactory.alwaysGrantAndMockGroups(Role.CLIENT),
                    authorizationProvider = AuthorizationProvider.configureRoleBased {
                        addPermission(Role.CONFIGURATOR, GoalUseCases.Tags.getAllGoalsIndex)
                    }
                )
            ) {
                client.get(goalBaseUrl)
                    .map { it.statusCode() shouldBe 403 }
                    .await()
            }
        }
        test("it should return 200 if the user has it") {
            withHttpServerOf(
                makeControllerOf(
                    authenticationHandler = AuthenticationHandlerTestFactory
                        .alwaysGrantAndMockGroups(Role.CONFIGURATOR),
                    authorizationProvider = AuthorizationProvider.configureRoleBased {
                        addPermission(Role.CONFIGURATOR, GoalUseCases.Tags.getAllGoalsIndex)
                    }
                )
            ) {
                client.get(goalBaseUrl)
                    .map { it.statusCode() shouldBe 200 }
                    .await()
            }
        }
    }
})
