plugins {
    id("kotlin-common-convention")
    id("application")
}

dependencies {
    implementation(project(":authentication-vertx"))
    implementation(project(":serialization-vertx"))
    implementation(project(":client-api"))
    implementation("it.unibo.tuprolog", "parser-core", "0.20.4")
    implementation("it.unibo.tuprolog", "parser-theory", "0.20.4")
    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin:4.2.1")
    implementation("com.github.ajalt.clikt:clikt:3.4.0")
}

application {
    mainClass.set("it.unibo.lpaas.client.application.ApplicationKt")
}
