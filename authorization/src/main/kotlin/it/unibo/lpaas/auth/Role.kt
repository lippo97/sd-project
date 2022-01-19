package it.unibo.lpaas.auth

enum class Role(val value: String) {
    CLIENT("client"),
    CONFIGURATOR("configurator"),
    SOURCE("source");

    companion object {
        @Throws(IllegalArgumentException::class)
        fun parse(input: String): Role = values().first { it.value == input }
    }
}
