plugins {
    id("kotlin-common-convention")
}

dependencies {
    api(project(":core"))
    api(project(":authorization"))
    api(project(":serialization"))
    api(project(":serialization-vertx"))
    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
    implementation("io.vertx:vertx-auth-jwt:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
    implementation("it.unibo.tuprolog", "solve", "0.20.4")

    testImplementation(testFixtures(project(":test-fixtures-domain")))
    testImplementation(testFixtures(project(":test-lib-vertx")))
    testImplementation("it.unibo.tuprolog", "parser-core", "0.20.4")
    testImplementation("it.unibo.tuprolog", "parser-theory", "0.20.4")
    testImplementation("it.unibo.tuprolog", "solve-classic", "0.20.4")
    testImplementation("io.mockk:mockk:1.12.1")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
}
