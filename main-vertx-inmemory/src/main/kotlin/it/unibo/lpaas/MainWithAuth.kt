package it.unibo.lpaas

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler
import it.unibo.lpaas.auth.AuthorizationProvider
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
import it.unibo.lpaas.delivery.http.auth.InMemoryTokenStorage
import it.unibo.lpaas.delivery.http.auth.JWTAuthFactory
import it.unibo.lpaas.delivery.http.bindApi
import it.unibo.lpaas.delivery.http.handler.AuthController
import it.unibo.lpaas.delivery.timer.vertx
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.http.databind.SerializerCollection
import it.unibo.lpaas.http.databind.SerializerConfiguration
import it.unibo.lpaas.persistence.inMemory
import it.unibo.tuprolog.solve.classic.ClassicSolverFactory

class MainWithAuth private constructor() {
    companion object {
        @JvmStatic
        @Suppress("MagicNumber", "SpreadOperator", "LongMethod")
        fun main(args: Array<String>) {
            val serializerCollection = SerializerCollection.default()

            SerializerConfiguration.defaultWithModule {
                addAbstractTypeMapping(IncrementalVersion::class.java, IntegerIncrementalVersion::class.java)
                addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
                addAbstractTypeMapping(TheoryId::class.java, StringId::class.java)
                addAbstractTypeMapping(SolutionId::class.java, StringId::class.java)
            }
                .applyOnJacksonAndSerializers(serializerCollection)

            val vertx = Vertx.vertx()
            val timer = Timer.vertx(vertx)
            val jwtProvider = JWTAuthFactory.hs256SecretBased(vertx, "keyboard cat")

            val controller = Controller.make(
                DependencyGraph(
                    vertx = vertx,
                    timerDependencies = TimerDependencies(
                        timer = timer,
                        timerRepository = TimerRepository.inMemory(),
                    ),
                    serializerCollection = serializerCollection,
                    authOptions = Controller.AuthOptions(
                        authenticationHandler = JWTAuthHandler.create(jwtProvider),
                        authorizationProvider = AuthorizationProvider.default(),
                    ),
                    goalDependencies = GoalDependencies(
                        goalRepository = GoalRepository.inMemory(),
                        goalIdParser = GoalId::of,
                    ),
                    theoryDependencies = TheoryDependencies(
                        theoryRepository = TheoryRepository.inMemory() { IncrementalVersion.zero },
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

            InMemoryTokenStorage.fromPropertyFile(
                vertx,
                this::class.java.classLoader.getResourceAsStream("tokens.properties")!!
            )
                .onFailure { it.printStackTrace() }
                .onFailure { println("Couldn't load token properties file.") }
                .flatMap { tokenStorage ->
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
                        .listen(8080).onComplete {
                            println("Running...")
                        }
                }
        }
    }
}
