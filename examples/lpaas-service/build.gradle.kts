plugins {
    id("kotlin-common-convention")
    application
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":delivery-vertx"))
    implementation(project(":authentication-vertx"))
    implementation(project(":persistence-mongo"))

    implementation(libs.vertx.core)
    implementation(libs.vertx.web)
    implementation(libs.vertx.auth.jwt)
    implementation(libs.vertx.lang.kotlin.coroutines)
    implementation(libs.tuprolog.solve.classic)
    implementation(libs.bundles.kmongo)
}

application {
    mainClass.set("it.unibo.lpaas.MainKt")
}
