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

//tasks.withType<Test> {
//    this.environment["kotest.tags"] = "!Mongo"
//}
