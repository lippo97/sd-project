package it.unibo.lpaas

object Environment {

    fun getString(name: String): String =
        System.getenv(name) ?: throw EnvironmentVariableException(name)

    fun getInt(name: String): Int =
        getString(name).toInt()
}
