plugins {
    id("kotlin-common-convention")
}

dependencies {
    api(project(":domain"))
    api("io.vertx:vertx-core:4.2.1")
    implementation(project(":serialization-vertx"))
    implementation(project(":authentication-vertx"))

    testImplementation(project(":utils"))
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
