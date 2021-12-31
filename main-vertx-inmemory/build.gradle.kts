plugins {
    id("kotlin-common-convention")
    `jvm-test-convention`
    application
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":domain"))
    implementation(project(":serialization"))
    implementation(project(":core"))
    implementation(project(":auth"))
    implementation(project(":delivery-vertx"))
    implementation(project(":persistence-inmemory"))

    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.0")

    testImplementation(testFixtures(project(":test-lib-vertx")))
    testImplementation("io.vertx:vertx-lang-kotlin:4.2.1")
    testImplementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
}

application {
    mainClass.set("it.unibo.lpaas.MainKt")
}
