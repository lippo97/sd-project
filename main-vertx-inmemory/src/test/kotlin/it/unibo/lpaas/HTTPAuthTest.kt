package it.unibo.lpaas

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.handler.AuthenticationHandler
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.RBAC
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.GoalUseCases
import it.unibo.lpaas.core.Tag
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.Parsers
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerFactory
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerTestFactory
import it.unibo.lpaas.delivery.http.databind.MimeMap
import it.unibo.lpaas.delivery.http.databind.MimeType
import it.unibo.lpaas.delivery.http.databind.ObjectMapperSerializer
import it.unibo.lpaas.delivery.http.get
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.databind.DomainSerializationModule
import it.unibo.lpaas.domain.databind.configureMappers
import it.unibo.lpaas.domain.impl.IncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.InMemoryGoalRepository

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

    fun makeControllerOf(authenticationHandler: AuthenticationHandler, rbac: RBAC): Controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            mimeMap = MimeMap.of(
                MimeType.JSON to jsonSerializer,
            ),
            authenticationHandler = authenticationHandler,
            goalRepository = InMemoryGoalRepository(),
            parsers = Parsers(
                goalIdParser = GoalId::of
            ),
            rbac = rbac,
        )
    )

    suspend fun withHttpServerOf(controller: Controller, fn: suspend () -> Unit) {
        with(
            vertx.createHttpServer()
                .requestHandler(
                    controller.routes()
                )
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
                    rbac = RBAC.alwaysGrant()
                )
            ) {
                client.get(Controller.GOAL_BASEURL)
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
                    rbac = RBAC.alwaysGrant()
                )
            ) {
                client.get(Controller.GOAL_BASEURL)
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
                    rbac = RBAC.configure {
                        addPermission(Role.CONFIGURATOR, GoalUseCases.Tags.getAllGoals)
                    }
                )
            ) {
                client.get(Controller.GOAL_BASEURL)
                    .map { it.statusCode() shouldBe 403 }
                    .await()
            }
        }
        test("it should return 403 if the user doesn't have it") {
            withHttpServerOf(
                makeControllerOf(
                    authenticationHandler = AuthenticationHandlerTestFactory.alwaysGrantAndMockGroups(Role.CLIENT),
                    rbac = RBAC.configure {
                        addPermission(Role.CONFIGURATOR, GoalUseCases.Tags.getAllGoalsIndex)
                    }
                )
            ) {
                client.get(Controller.GOAL_BASEURL)
                    .map { it.statusCode() shouldBe 403 }
                    .await()
            }
        }
        test("it should return 200 if the user has it") {
            withHttpServerOf(
                makeControllerOf(
                    authenticationHandler = AuthenticationHandlerTestFactory
                        .alwaysGrantAndMockGroups(Role.CONFIGURATOR),
                    rbac = RBAC.configure {
                        addPermission(Role.CONFIGURATOR, GoalUseCases.Tags.getAllGoalsIndex)
                    }
                )
            ) {
                client.get(Controller.GOAL_BASEURL)
                    .map { it.statusCode() shouldBe 200 }
                    .await()
            }
        }
    }
})
