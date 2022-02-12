package it.unibo.lpaas.client.mio

import it.unibo.lpaas.domain.Result

interface LpaasSolver : Solver<String, Result> {

//    companion object {
//        fun create(lpaas: Lpaas, theory: Theory): Future<LpaasSolver> {
//            return lpaas.createTheory(
//                TheoryId.of("aTheory"), TheoryData(theory)
//            )
//                .map { LpaasSolverImpl(lpaas) }
//            val body = JsonObject()
//                .put("name", "defaultTheory")
//                .put("value", "parent(goku, gohan).")
//                .toString()
//            println(body)
//            return client.request(HttpMethod.POST, lpaasOptions.port, lpaasOptions.hostname, "/v1/theories")
//                .flatMap {
//                    it.send(
// //                        JsonObject()
// //                            .put("name", "defaultTheory")
// //                            .put("data", JsonObject().put("value", theory.toString()))
// //                            .toString()
//                        body
//                    )
//                }
//                .map { it.statusCode() }
//                .map { statusCode ->
//                    if (statusCode != 201)
//                        throw RuntimeException("Couldn't create the theory instance. HTTP status code = $statusCode")
//                    else LpaasSolverImpl(client, lpaasOptions)
//                }
//        }
//    }
}
