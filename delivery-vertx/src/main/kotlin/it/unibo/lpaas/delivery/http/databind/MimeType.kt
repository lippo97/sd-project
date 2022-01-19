package it.unibo.lpaas.delivery.http.databind

enum class MimeType(val value: String) {
    JSON("application/json"),
    YAML("application/yaml"),
    XML("application/xml");

    companion object {
        @Throws(IllegalArgumentException::class)
        fun parse(input: String): MimeType = values().first { it.value == input }
    }
}
