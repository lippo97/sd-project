plugins {
    id("kotlin-common-convention")
    id("java-test-fixtures")
}

dependencies {
    testFixturesImplementation(project(":authorization"))
    testFixturesImplementation(libs.vertx.web)
    testFixturesImplementation(libs.bundles.vertx.kotlin)
}
