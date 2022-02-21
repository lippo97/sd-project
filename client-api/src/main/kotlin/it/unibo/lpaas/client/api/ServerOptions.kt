package it.unibo.lpaas.client.api

data class ServerOptions(
    val hostname: String,
    val port: Int,
    val baseUrl: String = ""
)
