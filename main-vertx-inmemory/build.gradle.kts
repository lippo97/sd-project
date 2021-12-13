plugins {
    id("kotlin-common-convention")
    application
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":delivery-vertx"))
    implementation(project(":persistence-inmemory"))

    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
}

application {
    mainClass.set("it.unibo.lpaas.MainKt")
}
