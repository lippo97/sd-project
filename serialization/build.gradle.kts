plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.tuprolog.parser.core)
    implementation(libs.tuprolog.parser.theory)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.dataformat.xml)

    testImplementation(libs.jackson.module.kotlin)
}
