package it.unibo.lpaas.auth

enum class Role(val value: String) {
    CLIENT("client"),
    CONFIGURATOR("configurator"),
    SOURCE("source"),
}
