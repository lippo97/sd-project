plugins {
    id("kotlin-common-convention")
}

dependencies {
    api(project(":domain"))
    api(project(":authentication"))
    implementation(project(":serialization-vertx"))
    api(libs.vertx.core)

    testImplementation(project(":utils"))
    testImplementation(project(":delivery-vertx"))
    testImplementation(project(":authentication-vertx"))
    testImplementation(project(":core"))
    testImplementation(project(":authorization"))
    testImplementation(project(":persistence-inmemory"))
    testImplementation(libs.mockk)
    testImplementation(libs.vertx.web)
    testImplementation(libs.vertx.auth.jwt)
    testImplementation(libs.bundles.vertx.kotlin)
    testImplementation(libs.tuprolog.solve.classic)
}
