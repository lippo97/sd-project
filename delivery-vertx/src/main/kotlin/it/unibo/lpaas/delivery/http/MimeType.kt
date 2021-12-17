package it.unibo.lpaas.delivery.http

enum class MimeType(val value: String) {
    JSON("application/json"),
    YAML("application/yaml");

    companion object {
        @Throws(NoSuchElementException::class)
        fun parse(input: String): MimeType = values().first { it.value == input }
    }
}