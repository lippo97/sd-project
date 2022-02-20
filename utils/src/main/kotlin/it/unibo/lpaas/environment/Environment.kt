package it.unibo.lpaas.environment

object Environment {
    object Config {
        val PRODUCTION by lazy { getNullableString("MODE")?.lowercase() == "production" }

        val DEVELOPMENT by lazy { !PRODUCTION }
    }
    object Web {
        val PORT by lazy { getNullableInt("LPAAS_WEB_PORT") }
    }

    object Mongo {
        val CONNECTION_STRING by lazy { Environment.getString("LPAAS_MONGO_CONNECTION_STRING") }
        val APPLICATION_DATABASE by lazy { Environment.getString("LPAAS_MONGO_DATABASE") }
    }

    object Secrets {
        val JWT_SECRET by lazy { getString("LPAAS_JWT_SECRET") }
    }

    fun getNullableString(name: String): String? = System.getenv(name)

    fun getString(name: String): String =
        getNullableString(name) ?: throw EnvironmentVariableException(name)

    fun getNullableInt(name: String): Int? = getNullableString(name)?.toInt()

    @Suppress("UnusedPrivateMember")
    fun getInt(name: String): Int =
        getNullableInt(name) ?: throw EnvironmentVariableException(name)
}
