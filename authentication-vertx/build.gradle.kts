plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":authorization"))
    api(project(":delivery-vertx"))
    implementation(libs.vertx.core)
    implementation(libs.vertx.web)
    implementation(libs.vertx.auth.jwt)
    implementation(libs.bundles.kmongo)
    implementation(libs.bcrypt)

    testImplementation(libs.bundles.vertx.kotlin)
}
