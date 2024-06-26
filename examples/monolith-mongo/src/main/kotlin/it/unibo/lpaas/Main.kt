package it.unibo.lpaas

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.authentication.AuthController
import it.unibo.lpaas.authentication.JWTAuthFactory
import it.unibo.lpaas.authentication.bcrypt.BCrypt
import it.unibo.lpaas.authentication.provider.CredentialsProvider
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
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.environment.Environment
import it.unibo.lpaas.persistence.mongo
import it.unibo.tuprolog.solve.classic.ClassicSolverFactory

fun main() {
    val vertx = Vertx.vertx()
    val timer = Timer.vertx(vertx)
    val bCrypt = BCrypt.vertx(vertx)
    val jwtProvider = JWTAuthFactory.hs256SecretBased(vertx, Environment.getString("LPAAS_JWT_SECRET"))

    val controller = Controller.make(
        DependencyGraph(
            vertx = vertx,
            timerDependencies = TimerDependencies(
                timer = timer,
                timerRepository = TimerRepository.mongo(Mongo.timerCollection)
            ),
            serializerCollection = Serializers.serializerCollection,
            authOptions = Controller.AuthOptions(
                authenticationHandler = JWTAuthHandler.create(jwtProvider),
                authorizationProvider = AuthorizationProvider.default()
            ),
            goalDependencies = GoalDependencies(
                goalRepository = GoalRepository.mongo(Mongo.goalRepository),
                goalIdParser = GoalId::of,
            ),
            theoryDependencies = TheoryDependencies(
                theoryRepository = TheoryRepository.mongo(Mongo.theoryRepository) { IncrementalVersion.zero },
                theoryIdParser = TheoryId::of,
                functorParser = { Functor(it) },
                incrementalVersionParser = { IncrementalVersion.of(Integer.parseInt(it))!! },
            ),
            solutionDependencies = SolutionDependencies(
                solutionRepository = SolutionRepository.mongo(Mongo.solutionRepository) { IncrementalVersion.zero },
                solutionIdParser = SolutionId::of,
                solutionIdGenerator = StringId::uuid,
                solverFactory = ClassicSolverFactory,
            )
        )
    )

    val credentialsProvider = CredentialsProvider.mongo(vertx, bCrypt, Mongo.userCollection)

    @Suppress("MagicNumber")
    vertx.createHttpServer()
        .requestHandler(
            Router.router(vertx).apply {
                bindApi(1, controller)
                mountSubRouter(
                    "/",
                    AuthController.make(vertx, jwtProvider, credentialsProvider).routes()
                )
            }
        )
        .listen(Environment.getNullableInt("LPAAS_PORT") ?: 8080).onComplete {
            println("Running...")
        }
}
