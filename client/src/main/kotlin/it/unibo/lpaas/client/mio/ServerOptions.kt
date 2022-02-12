package it.unibo.lpaas.client.mio

data class ServerOptions(
    val hostname: String,
    val port: Int,
    val baseUrl: String = ""
)
