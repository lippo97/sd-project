package it.unibo.lpaas.client.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.Json
import io.vertx.core.streams.ReadStream
import it.unibo.lpaas.client.streams.BoundedStream
import it.unibo.lpaas.client.streams.map
import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.Result
import it.unibo.lpaas.domain.Solution
import it.unibo.lpaas.domain.SolutionId
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId

@Suppress("TooGenericExceptionThrown")
class LpaasImpl(
    private val vertx: Vertx,
    private val client: HttpClient,
    private val serverOptions: ServerOptions,
    authenticationToken: String,
) : Lpaas,
    JwtTokenAuthentication by JwtTokenAuthentication.usingToken(client, serverOptions, authenticationToken) {

    override fun findTheoryByName(name: TheoryId): Future<Theory> =
        sendRequest(null, "/theories/${name.show()}", HttpMethod.GET)
            .map { Json.decodeValue(it, Theory::class.java) }

    override fun createTheory(name: TheoryId, data: Theory.Data): Future<Theory> =
        sendRequest(CreateTheoryDTO(name, data), "/theories", HttpMethod.POST)
            .map { Json.decodeValue(it, Theory::class.java) }

    override fun createGoal(name: GoalId, data: Goal.Data): Future<Goal> =
        sendRequest(CreateGoalDTO(name, data), "/goals", HttpMethod.POST)
            .map { Json.decodeValue(it, Goal::class.java) }

    override fun createSolution(name: SolutionId?, data: Solution.Data): Future<Solution> =
        sendRequest(CreateSolutionDTO(name, data), "/solutions", HttpMethod.POST)
            .map { Json.decodeValue(it, Solution::class.java) }

    override fun getResults(name: SolutionId): Pair<ReadStream<Result>, () -> Future<Void>> {
        val results = BoundedStream<Result>(vertx)

        val ws = client.webSocket(
            serverOptions.port,
            serverOptions.hostname,
            "${serverOptions.baseUrl}/solutions/${name.show()}/results"
        ).map { ws ->
            ws
                .map { Json.decodeValue(it, Result::class.java) }
                .pipeTo(results)
            ws
        }

        val getNext = { ws.flatMap { it.write(Buffer.buffer("get")) } }

        return Pair(results, getNext)
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
                    throw RuntimeException("HTTP status code = ${res.statusCode()}")
                } else res.body()
            }

    companion object {
        data class CreateTheoryDTO(val name: TheoryId, val data: Theory.Data)

        data class CreateGoalDTO(val name: GoalId, val data: Goal.Data)

        data class CreateSolutionDTO(val name: SolutionId?, val data: Solution.Data)
    }
}
