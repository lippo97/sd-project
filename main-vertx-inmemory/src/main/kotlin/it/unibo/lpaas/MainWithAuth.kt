package it.unibo.lpaas

import io.vertx.core.Vertx
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.DependencyGraph
import it.unibo.lpaas.delivery.http.GoalDependencies
import it.unibo.lpaas.delivery.http.TheoryDependencies
import it.unibo.lpaas.delivery.http.auth.InMemoryTokenStorage
import it.unibo.lpaas.delivery.http.bindApi
import it.unibo.lpaas.delivery.http.databind.SerializerCollection
import it.unibo.lpaas.delivery.http.databind.SerializerConfiguration
import it.unibo.lpaas.delivery.http.handler.AuthController
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.lpaas.persistence.ext.inMemory

class MainWithAuth private constructor() {
    companion object {
        @JvmStatic
        @Suppress("MagicNumber", "SpreadOperator", "LongMethod")
        fun main(args: Array<String>) {
            val serializerCollection = SerializerCollection.default()

            SerializerConfiguration.defaultWithModule {
                addAbstractTypeMapping(Version::class.java, IntegerIncrementalVersion::class.java)
                addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
            }
                .applyOnJacksonAndSerializers(serializerCollection)

            val vertx = Vertx.vertx()

            val jwtProvider = JWTAuth.create(
                vertx,
                JWTAuthOptions()
                    .addPubSecKey(
                        PubSecKeyOptions()
                            .setAlgorithm("HS256")
                            .setBuffer("keyboard cat")
                    )
            )

            val controller = Controller.make(
                DependencyGraph(
                    vertx = vertx,
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
                        theoryRepository = TheoryRepository.inMemory { IncrementalVersion.zero },
                        theoryIdParser = TheoryId::of,
                        functorParser = { Functor(it) },
                        incrementalVersionParser = { IncrementalVersion.of(Integer.parseInt(it))!! },
                    ),
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
                                bindApi(2, controller)
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
