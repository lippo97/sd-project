package io.unibo.lpaas.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.beEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.client.api.JwtTokenAuthentication
import it.unibo.lpaas.client.api.Lpaas
import it.unibo.lpaas.client.api.ServerOptions
import it.unibo.lpaas.client.api.exception.UnauthorizedException
import it.unibo.lpaas.collections.nonEmptyListOf
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
import it.unibo.lpaas.delivery.http.auth.JWTAuthFactory
import it.unibo.lpaas.delivery.http.auth.Token
import it.unibo.lpaas.delivery.http.auth.TokenStorage
import it.unibo.lpaas.delivery.http.auth.inMemory
import it.unibo.lpaas.delivery.http.bindApi
import it.unibo.lpaas.delivery.http.handler.AuthController
import it.unibo.lpaas.delivery.timer.vertx
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.SerializerCollection
import it.unibo.lpaas.http.databind.SerializerConfiguration
import it.unibo.lpaas.persistence.inMemory
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.theory.Theory
import it.unibo.lpaas.domain.Theory as MyTheory
import it.unibo.lpaas.domain.Theory.Data as TheoryData

suspend fun doAsync(fn: (done: () -> Unit) -> Unit) {
    val isComplete = Promise.promise<Unit>()
    fn(isComplete::complete)
    isComplete.future().await()
}

@Tags("HTTP")
class LpaasIntegrationTest : FunSpec({
    val vertx = Vertx.vertx()

    val httpClient = vertx.createHttpClient()
    val timer = Timer.vertx(vertx)
    val serializers = SerializerCollection.default()

    SerializerConfiguration.defaultWithModule {
        addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
        addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
        addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
        addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
    }.applyOnJacksonAndSerializers(serializers)

    val jwtProvider = JWTAuthFactory.hs256SecretBased(vertx, "keyboard cat")

    val controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            timerDependencies = TimerDependencies(
                timer = timer,
                timerRepository = TimerRepository.inMemory(),
            ),
            serializerCollection = serializers,
            authOptions = Controller.AuthOptions(
                authenticationHandler = JWTAuthHandler.create(jwtProvider),
                authorizationProvider = AuthorizationProvider.default(),
            ),
            goalDependencies = GoalDependencies(
                goalRepository = GoalRepository.inMemory(),
                goalIdParser = GoalId::of,
            ),
            theoryDependencies = TheoryDependencies(
                theoryRepository = TheoryRepository.inMemory(
                    mapOf(
                        TheoryId.of("initialTheory") to nonEmptyListOf(
                            MyTheory(
                                TheoryId.of("initialTheory"),
                                TheoryData(Theory.empty()),
                                IncrementalVersion.zero
                            )
                        )
                    ),
                ) { IncrementalVersion.zero },
                theoryIdParser = TheoryId::of,
                functorParser = { Functor(it) },
                incrementalVersionParser = { IncrementalVersion.of(Integer.parseInt(it))!! },
            ),
            solutionDependencies = SolutionDependencies(
                solutionRepository = SolutionRepository.inMemory { IncrementalVersion.zero },
                solutionIdParser = SolutionId::of,
                solutionIdGenerator = StringId::uuid,
                solverFactory = Solver.prolog,
            )
        )
    )

    val tokenStorage = TokenStorage.inMemory(
        Token("abc") to Role.CONFIGURATOR
    )

    val lpaas = Lpaas.of(
        vertx,
        httpClient,
        ServerOptions("localhost", 8090, "/v1"),
        "abc"
    )

    beforeAny {
        vertx.createHttpServer()
            .requestHandler(
                Router.router(vertx).apply {
                    bindApi(1, controller)
                    mountSubRouter(
                        "/",
                        AuthController.make(vertx, jwtProvider, tokenStorage).routes()
                    )
                }
            )
            .listen(8090)
            .await()
    }

    context("HTTP E2E tests") {
        context("login") {
            test("it should authenticate as CONFIGURATOR") {
                val jwtTokenAuthentication = JwtTokenAuthentication
                    .usingToken(
                        httpClient,
                        ServerOptions("localhost", 8090, "/v1"),
                        "abc"
                    )
                jwtTokenAuthentication.getValidToken()
                    .await() shouldNotBe null
            }
            test("it should not authenticate with invalid token") {
                shouldThrow<UnauthorizedException> {
                    JwtTokenAuthentication
                        .usingToken(
                            httpClient,
                            ServerOptions("localhost", 8090, "/v1"),
                            "abce"
                        )
                        .getValidToken()
                        .await()
                }
            }
        }

        context("findTheoryByName") {
            test("it should find a theory") {
                lpaas.findTheoryByName(TheoryId.of("initialTheory"))
                    .map { it.name shouldBe TheoryId.of("initialTheory") }
                    .await()
            }
            test("it shouldn't find a theory") {
                shouldThrow<RuntimeException> {
                    lpaas.findTheoryByName(TheoryId.of("nonExistingOne"))
                        .await()
                }
            }
        }

        context("createTheory") {
            test("it should create a theory") {
                lpaas.createTheory(
                    TheoryId.of("exampleTheory"),
                    TheoryData(
                        Theory.of(
                            Clause.of(Struct.of("ciao", Atom.of("mario"))),
                            Clause.of(Struct.of("ciao", Atom.of("luigi"))),
                            Clause.of(Struct.of("ciao", Atom.of("peach"))),
                        )
                    )
                )
                    .map {
                        it.name shouldBe TheoryId.of("exampleTheory")
                        it.version shouldBe IncrementalVersion.zero
                        it.data.value.shouldNotBeEmpty()
                    }
                    .await()
            }
            test("it should fail") {
                shouldThrow<RuntimeException> {
                    lpaas.createTheory(TheoryId.of("exampleTheory"), TheoryData(Theory.empty()))
                        .await()
                }
            }
        }

        context("createGoal") {
            test("it should create a goal") {
                lpaas.createGoal(
                    GoalId.of("exampleGoal"),
                    Goal.Data(
                        listOf(
                            Subgoal(Struct.of("ciao", Var.anonymous())),
                        ),
                    ),
                )
                    .map {
                        it.name shouldBe GoalId.of("exampleGoal")
                    }
                    .await()
            }
            test("it should fail") {
                shouldThrow<RuntimeException> {
                    lpaas.createGoal(GoalId.of("exampleGoal"), Goal.Data(listOf(Subgoal(Struct.of("atom")))))
                        .await()
                }
            }
        }

        context("createSolution") {
            test("it should create a solution") {
                lpaas.createSolution(
                    SolutionId.of("exampleSolution"),
                    Solution.Data(
                        Solution.TheoryOptions(TheoryId.of("exampleTheory")),
                        GoalId.of("exampleGoal")
                    )
                ).map {
                    it.name.show() shouldNot beEmpty()
                }
                    .await()
            }
            test("it should fail") {
                shouldThrow<RuntimeException> {
                    lpaas.createSolution(
                        null,
                        Solution.Data(
                            Solution.TheoryOptions(TheoryId.of("nonExistingTheory")),
                            GoalId.of("exampleGoal")
                        )
                    )
                        .await()
                }
            }
        }

        context("getResults") {
            test("it should return no results") {
                lpaas.createGoal(
                    GoalId.of("failGoal"),
                    Goal.Data(
                        listOf(
                            Subgoal(Struct.of("hello", Var.anonymous())),
                        ),
                    ),
                )
                    .await()
                lpaas.createSolution(
                    SolutionId.of("failSolution"),
                    Solution.Data(
                        Solution.TheoryOptions(TheoryId.of("exampleTheory")),
                        GoalId.of("failGoal")
                    )
                )
                    .await()
                val results = lpaas.getResults(SolutionId.of("failSolution"))
                    .await()
                doAsync { done ->
                    results.handler {
                        it.shouldBeInstanceOf<Result.No>()
                        done()
                    }
                        .exceptionHandler { it.printStackTrace() }
                    results.next()
                }
            }
            test("it should return all the results") {
                val results = lpaas.getResults(SolutionId.of("exampleSolution"))
                    .await()
                doAsync { done ->
                    val resultList = mutableListOf<Result>()
                    results.handler {
                        if (it != null) {
                            resultList.add(it)
                        } else {
                            resultList shouldHaveSize 3
                            resultList.forEach { it.shouldBeInstanceOf<Result.Yes>() }
                            done()
                        }
                    }
                    repeat(4) {
                        results.next()
                    }
                }
            }
        }
    }
})
