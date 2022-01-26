package it.unibo.lpaas.delivery.auth

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.auth.AuthorizationProvider
import it.unibo.lpaas.auth.Role
import it.unibo.lpaas.core.persistence.GoalRepository
import it.unibo.lpaas.core.persistence.SolutionRepository
import it.unibo.lpaas.core.persistence.TheoryRepository
import it.unibo.lpaas.core.timer.Timer
import it.unibo.lpaas.core.timer.TimerRepository
import it.unibo.lpaas.delivery.Pokemon
import it.unibo.lpaas.delivery.http.Controller
import it.unibo.lpaas.delivery.http.auth.AuthenticationHandlerTestFactory
import it.unibo.lpaas.delivery.http.databind.SerializerCollection
import it.unibo.lpaas.delivery.http.databind.SerializerConfiguration
import it.unibo.lpaas.delivery.http.get
import it.unibo.lpaas.delivery.http.handler.SolutionController
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.TheoryId
import it.unibo.lpaas.domain.Version
import it.unibo.lpaas.domain.impl.IntegerIncrementalVersion
import it.unibo.lpaas.domain.impl.StringId
import it.unibo.tuprolog.solve.classic.ClassicSolverFactory
import org.junit.jupiter.api.Tag
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Tag("HTTP")
class SolutionControllerTest : DescribeSpec({

    val vertx = Vertx.vertx()
    val client = vertx.createHttpClient()

    val theoryRepository = mockk<TheoryRepository>()
    val goalRepository = mockk<GoalRepository>()
    val solutionRepository = mockk<SolutionRepository>()
    val timerRepository = mockk<TimerRepository<String>>()
    val timer = mockk<Timer<String>>()

    val serializerCollection = SerializerCollection.default()
    SerializerConfiguration.defaultWithModule {
        addAbstractTypeMapping(Version::class.java, IntegerIncrementalVersion::class.java)
        addAbstractTypeMapping(GoalId::class.java, StringId::class.java)
    }
        .applyOnJacksonAndSerializers(serializerCollection)
    val solutionController = SolutionController.make(
        vertx,
        goalRepository,
        theoryRepository,
        solutionRepository,
        timerRepository,
        SolutionId::of,
        { IncrementalVersion.of(Integer.parseInt(it))!! },
        timer,
        { SolutionId.of(UUID.randomUUID().toString()) },
        Controller.AuthOptions(
            authenticationHandler = AuthenticationHandlerTestFactory.alwaysGrantAndMockGroups(Role.CLIENT),
            authorizationProvider = AuthorizationProvider.alwaysGrant(),
        ),
        serializerCollection,
        ClassicSolverFactory,
    )

    describe("Websocket tests") {

        val theoryId = TheoryId.of("theoryId")
        val goalId = GoalId.of("goalId")
        val solutionId = SolutionId.of("solutionId")
        val solution = Solution(
            solutionId,
            Solution.Data(
                Solution.TheoryOptions(theoryId), goalId
            ),
            IncrementalVersion.zero
        )

        it("it should create the websocket") {
            vertx.createHttpServer()
                .requestHandler(solutionController.routes())
                .listen(8085)
                .await()
        }
        it("it should return bad request") {
            client.get("/mySolution/results", "localhost", 8085)
                .map { it.statusCode() shouldBe 400 }
                .await()
        }

        it("it should connect to the websocket").config(timeout = 5.seconds) {
            val promise = Promise.promise<List<String>>()
            coEvery { solutionRepository.findByName(any()) } returns solution
            coEvery { theoryRepository.findByName(theoryId) } returns Pokemon.theory
            coEvery { goalRepository.findByName(goalId) } returns Pokemon.Goals.intermediateLevelPokemon

            client.webSocket(8085, "localhost", "/mySolution/results") { res ->
                res.succeeded() shouldBe true
                val ws = res.result()
                val pokemons = mutableListOf<String>()
                ws.write(Buffer.buffer("get"))
                ws
                    .handler {
                        pokemons.add(
                            it.toJsonObject()
                                ?.getJsonObject("variables")
                                ?.getString("Pokemon") ?: "NO"
                        )
                        if (!ws.isClosed) ws.write(Buffer.buffer("get"))
                    }
                    .closeHandler {
                        promise.complete(pokemons.dropLast(1))
                    }
            }
            promise.future()
                .await()
                .shouldContainExactly("charmeleon", "wartortle", "ivysaur")
        }
    }
})
