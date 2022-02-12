plugins {
    id("kotlin-common-convention")
    id("application")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":serialization-vertx"))
    implementation("it.unibo.tuprolog", "solve", "0.20.4")

    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin:4.2.1")
    implementation("it.unibo.tuprolog", "parser-core", "0.20.4")
    implementation("it.unibo.tuprolog", "parser-theory", "0.20.4")


    testImplementation(project(":delivery-vertx"))
    testImplementation(project(":core"))
    testImplementation(project(":authorization"))
    testImplementation(project(":persistence-inmemory"))
    testImplementation("io.mockk:mockk:1.12.1")
    testImplementation("io.vertx:vertx-web:4.2.1")
    testImplementation("io.vertx:vertx-auth-jwt:4.2.1")
    testImplementation("io.vertx:vertx-lang-kotlin:4.2.1")
    testImplementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
    testImplementation("it.unibo.tuprolog", "solve-classic", "0.20.4")
}

application {
    mainClass.set("it.unibo.lpaas.client.MainKt")
}
