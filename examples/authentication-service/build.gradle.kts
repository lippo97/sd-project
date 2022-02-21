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

    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
    implementation("io.vertx:vertx-auth-jwt:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
    implementation("org.litote.kmongo:kmongo:4.4.0")
    implementation("org.litote.kmongo:kmongo-async:4.4.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.4.0")
}

application {
    mainClass.set("it.unibo.lpaas.authentication.AuthenticationServiceKt")
}
