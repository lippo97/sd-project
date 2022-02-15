plugins {
    id("kotlin-common-convention")
    application
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":domain"))
    implementation(project(":serialization"))
    implementation(project(":core"))
    implementation(project(":authorization"))
    implementation(project(":delivery-vertx"))
    implementation(project(":persistence-mongo"))

    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
    implementation("io.vertx:vertx-auth-jwt:4.2.1")

    implementation("it.unibo.tuprolog", "solve-classic", "0.20.4")

    implementation("org.litote.kmongo:kmongo:4.4.0")
    implementation("org.litote.kmongo:kmongo-async:4.4.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.4.0")
}

distributions {
}

application {
    mainClass.set("it.unibo.lpaas.MainKt")
}
