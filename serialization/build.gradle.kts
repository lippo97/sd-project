plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":domain"))
    implementation("it.unibo.tuprolog", "parser-core", "0.20.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.0")


    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
}
