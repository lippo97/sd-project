package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.Parsers
import it.unibo.lpaas.delivery.http.Repositories
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerFactory
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerTestFactory
import it.unibo.lpaas.delivery.http.bindAPIVersion
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.delivery.http.databind.MimeType
import it.unibo.lpaas.delivery.http.databind.ObjectMapperSerializer
import it.unibo.lpaas.delivery.http.get
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.databind.DomainSerializationModule
import it.unibo.lpaas.domain.databind.configureMappers
import it.unibo.lpaas.domain.impl.IncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.InMemoryGoalRepository

@Tags("HTTP")
class HTTPAuthTest : FunSpec({

    val jsonSerializer = ObjectMapperSerializer.json()
    configureMappers(
        DatabindCodec.mapper(),
        DatabindCodec.prettyMapper(),
        jsonSerializer.objectMapper,
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

    val vertx = Vertx.vertx()
    val client = vertx.createHttpClient()
    val goalBaseUrl = Controller.API_VERSION + Controller.GOAL_BASEURL

    fun makeControllerOf(
        authenticationHandler: AuthenticationHandler,
        authorizationProvider: AuthorizationProvider
    ): Controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            mimeMap = MimeMap.of(
                MimeType.JSON to jsonSerializer,
            ),
            authenticationHandler = authenticationHandler,
            repositories = Repositories(
                goalRepository = InMemoryGoalRepository(),
            ),
            parsers = Parsers(
                goalIdParser = GoalId::of
            ),
            authorizationProvider = authorizationProvider,
        )
    )

    suspend fun withHttpServerOf(controller: Controller, fn: suspend () -> Unit) {
        with(
            vertx.createHttpServer()
                .bindAPIVersion(1, controller, vertx)
        ) {
            listen(8080).await()
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
