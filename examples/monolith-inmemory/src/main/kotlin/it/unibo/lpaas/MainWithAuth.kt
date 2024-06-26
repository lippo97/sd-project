package it.unibo.lpaas

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.authentication.AuthController
import it.unibo.lpaas.authentication.JWTAuthFactory
import it.unibo.lpaas.authentication.bcrypt.BCrypt
import it.unibo.lpaas.authentication.domain.Password
import it.unibo.lpaas.authentication.domain.Username
import it.unibo.lpaas.authentication.provider.InMemoryCredentialsProvider
import it.unibo.lpaas.authentication.serialization.PasswordDeserializer
import it.unibo.lpaas.authentication.serialization.RoleDeserializer
import it.unibo.lpaas.authentication.serialization.UsernameDeserializer
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
import it.unibo.lpaas.delivery.http.bindApi
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
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

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
                addDeserializer(Username::class.java, UsernameDeserializer())
                addDeserializer(Password::class.java, PasswordDeserializer())
                addDeserializer(Role::class.java, RoleDeserializer())
            }
                .applyOnJacksonAndSerializers(serializerCollection)

            val vertx = Vertx.vertx()
            val timer = Timer.vertx(vertx)
            val bCrypt = BCrypt.vertx(vertx)
            val jwtProvider = JWTAuthFactory.asymmetric(
                vertx,
                Files.readAllBytes(Paths.get("keys/public.pem")).toString(Charset.defaultCharset()),
                Files.readAllBytes(Paths.get("keys/private_key.pem")).toString(Charset.defaultCharset()),
            )

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

            InMemoryCredentialsProvider.fromJsonFile(
                vertx,
                bCrypt,
                this::class.java.classLoader.getResourceAsStream("credentials.json")!!
            )
                .onFailure { it.printStackTrace() }
                .onFailure { println("Couldn't load credentials properties file.") }
                .flatMap { credentialsProvider ->
                    vertx.createHttpServer()
                        .requestHandler(
                            Router.router(vertx).apply {
                                bindApi(1, controller)
                                mountSubRouter(
                                    "/",
                                    AuthController.make(vertx, jwtProvider, credentialsProvider, "RS256").routes()
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
