plugins {
    id("kotlin-common-convention")
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":authentication-vertx"))
    implementation(project(":serialization-vertx"))
    implementation(project(":client-api"))
    implementation(libs.tuprolog.parser.core)
    implementation(libs.tuprolog.parser.theory)
    implementation(libs.vertx.core)
    implementation(libs.vertx.lang.kotlin.core)
    implementation(libs.clikt)
}

val main = "it.unibo.lpaas.client.application.ApplicationKt"

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", main))
        }
    }
}

application {
    mainClass.set(main)
}
