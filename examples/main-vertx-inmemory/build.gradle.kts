plugins {
    id("kotlin-common-convention")
    `jvm-test-convention`
    application
}

dependencies {
    implementation(project(":utils"))
//    implementation(project(":core"))
    implementation(project(":serialization"))
    implementation(project(":authorization"))
    implementation(project(":delivery-vertx"))
    implementation(project(":authentication-vertx"))
    implementation(project(":persistence-inmemory"))
    implementation(libs.vertx.core)
    implementation(libs.vertx.web)
    implementation(libs.vertx.auth.jwt)
    implementation(libs.tuprolog.solve.classic)

    testImplementation(testFixtures(project(":test-lib-vertx")))
    testImplementation(libs.bundles.vertx.kotlin)
}

application {
    mainClass.set("it.unibo.lpaas.MainKt")
}
