plugins {
    id("kotlin-common-convention")
}

dependencies {
    api(project(":delivery-vertx"))
    api(project(":authentication"))
    implementation(libs.vertx.core)
    implementation(libs.vertx.web)
    implementation(libs.vertx.auth.jwt)
    implementation(libs.bundles.kmongo)

    testImplementation(libs.bundles.vertx.kotlin)
}
