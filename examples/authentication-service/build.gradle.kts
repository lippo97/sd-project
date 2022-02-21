plugins {
    id("kotlin-common-convention")
    application
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":authorization"))
    implementation(project(":authentication-vertx"))
    implementation(project(":serialization-vertx"))
    implementation(project(":persistence-mongo"))

    implementation(libs.vertx.core)
    implementation(libs.vertx.web)
    implementation(libs.vertx.auth.jwt)
    implementation(libs.vertx.lang.kotlin.coroutines)
    implementation(libs.bundles.kmongo)
}

application {
    mainClass.set("it.unibo.lpaas.authentication.AuthenticationServiceKt")
}
