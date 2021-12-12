plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
    implementation("io.vertx:vertx-web-validation:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")

    runtimeOnly("com.fasterxml.jackson.core:jackson-core:2.13.0")
    runtimeOnly("com.fasterxml.jackson.core:jackson-annotations:2.13.0")
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind:2.13.0")
}
