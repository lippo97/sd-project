plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":utils"))

    api(libs.tuprolog.core)
    api(libs.tuprolog.theory)
    implementation(libs.tuprolog.parser.core)
    implementation(libs.tuprolog.parser.theory)
    implementation(libs.tuprolog.solve.api)
}
