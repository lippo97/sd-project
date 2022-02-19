plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":authorization"))
    implementation(project(":delivery-vertx"))
    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
    implementation("io.vertx:vertx-auth-jwt:4.2.1")
    implementation("org.litote.kmongo:kmongo:4.4.0")
    implementation("org.litote.kmongo:kmongo-async:4.4.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.4.0")

    testImplementation("io.vertx:vertx-lang-kotlin:4.2.1")
    testImplementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
}
