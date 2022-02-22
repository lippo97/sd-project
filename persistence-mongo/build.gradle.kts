plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":serialization"))

    implementation(libs.tuprolog.serialize.core)
    implementation(libs.tuprolog.serialize.theory)
    implementation(libs.bundles.kmongo)

}

/*
 * Exclude MongoDB related tests by default, so that they won't be triggered
 * by default. They can be manually activated by overriding the "kotest.tags"
 * environment variable, e.g.:
 *
 * $ gradle test -Dkotest.tags=""
 */
tasks.withType<Test> {
    systemProperties["kotest.tags"] = systemProperties["kotest.tags"] ?: "!Mongo"
}
