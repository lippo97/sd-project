plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":auth"))
    implementation(project(":serialization"))
    implementation("io.vertx:vertx-core:4.2.1")
    implementation("io.vertx:vertx-web:4.2.1")
    implementation("io.vertx:vertx-auth-jwt:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin:4.2.1")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
    api("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.0")


    runtimeOnly("com.fasterxml.jackson.core:jackson-core:2.13.0")
    runtimeOnly("com.fasterxml.jackson.core:jackson-annotations:2.13.0")

    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
}
