package it.unibo.lpaas.environment

object Environment {
    object Config {
        val PRODUCTION by lazy { getNullableString("MODE")?.lowercase() == "production" }

        val DEVELOPMENT by lazy { !PRODUCTION }
    }

    fun getNullableString(name: String): String? = System.getenv(name)

    fun getString(name: String): String =
        getNullableString(name) ?: throw EnvironmentVariableException(name)

    fun getNullableInt(name: String): Int? = getNullableString(name)?.toInt()

    @Suppress("UnusedPrivateMember")
    fun getInt(name: String): Int =
        getNullableInt(name) ?: throw EnvironmentVariableException(name)
}
