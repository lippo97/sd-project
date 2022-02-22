plugins {
    id("kotlin-common-convention")
    id("application")
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

application {
    mainClass.set("it.unibo.lpaas.client.application.ApplicationKt")
}
