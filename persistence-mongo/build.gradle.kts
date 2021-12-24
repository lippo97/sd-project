plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":serialization"))

    implementation("it.unibo.tuprolog", "serialize-core", "0.20.4")
    implementation("org.litote.kmongo:kmongo:4.4.0")
    implementation("org.litote.kmongo:kmongo-async:4.4.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.4.0")
}

/*
 * Exclude MongoDB related tests by default, so that they won't be triggered
 * by default. They can be manually activated by overriding the "kotest.tags"
 * environment variable, e.g.:
 *
 * $ gradle test -Dkotest.tags=""
 */
tasks.withType<Test> {
    this.environment["kotest.tags"] = this.environment["kotest.tags"] ?: "!Mongo"
}

