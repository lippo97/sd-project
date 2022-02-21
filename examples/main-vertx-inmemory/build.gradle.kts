plugins {
    id("kotlin-common-convention")
    `jvm-test-convention`
    application
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":core"))
    implementation(project(":serialization"))
    implementation(project(":authorization"))
    implementation(project(":delivery-vertx"))
    implementation(project(":authentication-vertx"))
    implementation(project(":persistence-inmemory"))

    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
    implementation("io.vertx:vertx-auth-jwt:4.2.1")
    implementation("it.unibo.tuprolog", "solve-classic", "0.20.4")

    testImplementation(testFixtures(project(":test-lib-vertx")))
    testImplementation("io.vertx:vertx-lang-kotlin:4.2.1")
    testImplementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
}

application {
    mainClass.set("it.unibo.lpaas.MainKt")
}
