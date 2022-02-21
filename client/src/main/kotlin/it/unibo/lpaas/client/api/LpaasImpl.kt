package it.unibo.lpaas.client.api

import com.fasterxml.jackson.core.type.TypeReference
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.http.WebSocketConnectOptions
import io.vertx.core.json.Json
import io.vertx.core.json.jackson.DatabindCodec.mapper
import it.unibo.lpaas.authentication.domain.Credentials
import it.unibo.lpaas.domain.Fact
import it.unibo.lpaas.domain.Functor
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

@Suppress("TooGenericExceptionThrown", "TooManyFunctions")
class LpaasImpl(
    private val vertx: Vertx,
    private val client: HttpClient,
    private val serverOptions: ServerOptions,
    authServerOptions: ServerOptions,
    private val credentials: Credentials,
) : Lpaas,
    JwtTokenAuthentication by JwtTokenAuthentication.usingCredentials(client, authServerOptions, credentials) {
    override fun getAllGoalsIndex(): Future<List<GoalId>> =
        sendRequest(null, "/goals/", HttpMethod.GET)
            .map { mapper().readValue(it.toString(), object : TypeReference<List<GoalId>>() {}) }

    override fun getGoalByName(name: GoalId): Future<Goal> =
        sendRequest(null, "/goals/${name.show()}", HttpMethod.GET)
            .map { Json.decodeValue(it, Goal::class.java) }

    override fun createGoal(name: GoalId, data: Goal.Data): Future<Goal> =
        sendRequest(CreateGoalDTO(name, data), "/goals", HttpMethod.POST)
            .map { Json.decodeValue(it, Goal::class.java) }

    override fun replaceGoal(name: GoalId, data: Goal.Data): Future<Goal> =
        sendRequest(data, "/goals/${name.show()}", HttpMethod.PUT)
            .map { Json.decodeValue(it, Goal::class.java) }

    override fun deleteGoal(name: GoalId): Future<Unit> =
        sendRequest(null, "/goals/${name.show()}", HttpMethod.DELETE)
            .map { }

    override fun appendSubgoal(name: GoalId, subGoal: Subgoal): Future<Goal> =
        sendRequest(subGoal, "/goals/${name.show()}", HttpMethod.PATCH)
            .map { Json.decodeValue(it, Goal::class.java) }

    override fun getSubgoalByIndex(name: GoalId, index: Int): Future<Subgoal> =
        sendRequest(null, "/goals/${name.show()}/$index", HttpMethod.GET)
            .map { Json.decodeValue(it, Subgoal::class.java) }

    override fun replaceSubgoal(name: GoalId, index: Int, subGoal: Subgoal): Future<Goal> =
        sendRequest(subGoal, "/goals/${name.show()}/$index", HttpMethod.PUT)
            .map { Json.decodeValue(it, Goal::class.java) }

    override fun deleteSubgoal(name: GoalId, index: Int): Future<Unit> =
        sendRequest(null, "/goals/${name.show()}/$index", HttpMethod.DELETE)
            .map { }

    override fun getAllTheoriesIndex(): Future<List<TheoryId>> =
        sendRequest(null, "/theories/", HttpMethod.GET)
            .map {
                mapper().readValue(it.toString(), object : TypeReference<List<TheoryId>>() {})
            }

    override fun getTheoryByName(name: TheoryId): Future<Theory> =
        sendRequest(null, "/theories/${name.show()}", HttpMethod.GET)
            .map { Json.decodeValue(it, Theory::class.java) }

    override fun createTheory(name: TheoryId, data: Theory.Data): Future<Theory> =
        sendRequest(CreateTheoryDTO(name, data), "/theories", HttpMethod.POST)
            .map { Json.decodeValue(it, Theory::class.java) }

    override fun updateTheory(name: TheoryId, data: Theory.Data): Future<Theory> =
        sendRequest(data, "/theories/${name.show()}", HttpMethod.PUT)
            .map { Json.decodeValue(it, Theory::class.java) }

    override fun deleteTheory(name: TheoryId): Future<Unit> =
        sendRequest(null, "/theories/${name.show()}", HttpMethod.DELETE)
            .map { }

    override fun getFactsInTheory(name: TheoryId, functor: Functor): Future<List<Fact>> =
        sendRequest(null, "/theories/${name.show()}/facts/${functor.value}", HttpMethod.GET)
            .map {
                mapper().readValue(it.toString(), object : TypeReference<List<Fact>>() {})
            }

    override fun addFactToTheory(name: TheoryId, fact: Fact, beginning: Boolean): Future<Theory> =
        sendRequest(fact, "/theories/${name.show()}/facts?beginning=$beginning", HttpMethod.POST)
            .map { Json.decodeValue(it, Theory::class.java) }

    override fun updateFactInTheory(name: TheoryId, fact: Fact, beginning: Boolean): Future<Theory> =
        sendRequest(fact, "/theories/${name.show()}/facts?beginning=$beginning", HttpMethod.PUT)
            .map { Json.decodeValue(it, Theory::class.java) }

    override fun getTheoryByNameAndVersion(name: TheoryId, version: IncrementalVersion): Future<Theory> =
        sendRequest(null, "/theories/${name.show()}/history/${version.show()}", HttpMethod.GET)
            .map { Json.decodeValue(it, Theory::class.java) }

    override fun deleteTheoryByVersion(name: TheoryId, version: IncrementalVersion): Future<Unit> =
        sendRequest(null, "/theories/${name.show()}/history/${version.show()}", HttpMethod.DELETE)
            .map { }

    override fun getFactsInTheoryByNameAndVersion(
        name: TheoryId,
        functor: Functor,
        version: IncrementalVersion
    ): Future<List<Fact>> =
        sendRequest(null, "/theories/${name.show()}/history/${version.show()}/facts/${functor.value}", HttpMethod.GET)
            .map {
                mapper().readValue(it.toString(), object : TypeReference<List<Fact>>() {})
            }

    override fun getSolution(name: SolutionId): Future<Solution> =
        sendRequest(null, "/solutions/${name.show()}", HttpMethod.GET)
            .map { Json.decodeValue(it, Solution::class.java) }

    override fun getSolutionByVersion(name: SolutionId, version: IncrementalVersion): Future<Solution> =
        sendRequest(null, "/solutions/${name.show()}/history/${version.show()}", HttpMethod.GET)
            .map { Json.decodeValue(it, Solution::class.java) }

    override fun deleteSolution(name: SolutionId): Future<Unit> =
        sendRequest(null, "/solutions/${name.show()}", HttpMethod.DELETE)
            .map { }

    override fun createSolution(name: SolutionId?, data: Solution.Data): Future<Solution> =
        sendRequest(CreateSolutionDTO(name, data), "/solutions", HttpMethod.POST)
            .map { Json.decodeValue(it, Solution::class.java) }

    override fun getResults(name: SolutionId): Future<ResultStream> {
        val streamPromise = Promise.promise<ResultStream>()

        getValidToken()
            .flatMap { jwtToken ->
                client.webSocket(
                    WebSocketConnectOptions().apply {
                        port = serverOptions.port
                        host = serverOptions.hostname
                        uri = "${serverOptions.baseUrl}/solutions/${name.show()}/results"
                        headers = MultiMap.caseInsensitiveMultiMap()
                            .add(HttpHeaders.AUTHORIZATION, jwtToken.bearer())
                    }
                )
            }
            .map { ws ->
                ws.textMessageHandler {
                    if (it == "ready") {
                        vertx.executeBlocking<Unit> {
                            streamPromise.complete(ResultStream.of(ws))
                            it.complete()
                        }
                    }
                }
            }
            .onFailure { it.printStackTrace() }

        return streamPromise.future()
    }

    @Suppress("MagicNumber")
    private fun sendRequest(dto: Any?, path: String, httpMethod: HttpMethod): Future<Buffer> =
        getValidToken()
            .flatMap { jwtToken ->
                client.request(
                    RequestOptions().apply {
                        host = serverOptions.hostname
                        port = serverOptions.port
                        uri = "${serverOptions.baseUrl}$path"
                        method = httpMethod
                        headers = MultiMap.caseInsensitiveMultiMap()
                            .add(HttpHeaders.AUTHORIZATION, jwtToken.bearer())
                    }
                )
            }
            .flatMap { if (dto != null) it.send(Json.encodeToBuffer(dto)) else it.send() }
            .flatMap { res ->
                if (res.statusCode() == HttpResponseStatus.UNAUTHORIZED.code()) {
                    invalidateToken()
                    sendRequest(dto, path, httpMethod)
                } else if (res.statusCode() >= 300 || res.statusCode() < 200) {
                    throw RuntimeException("HTTP status code = ${res.statusCode()} ${res.statusMessage()}")
                } else res.body()
            }

    companion object {
        data class CreateTheoryDTO(val name: TheoryId, val data: Theory.Data)

        data class CreateGoalDTO(val name: GoalId, val data: Goal.Data)

        data class CreateSolutionDTO(val name: SolutionId?, val data: Solution.Data)
    }
}
