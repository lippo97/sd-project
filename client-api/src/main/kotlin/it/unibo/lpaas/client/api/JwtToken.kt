package it.unibo.lpaas.client.api

@JvmInline
value class JwtToken(val s: String) {
    fun bearer(): String = "Bearer $s"
}
