plugins {
    id("kotlin-common-convention")
    id("java-test-fixtures")
}

dependencies {
    testFixturesImplementation("io.vertx:vertx-lang-kotlin:4.2.1")
    testFixturesImplementation("io.vertx:vertx-lang-kotlin-coroutines:4.2.1")
}
