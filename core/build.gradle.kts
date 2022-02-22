plugins {
    `kotlin-common-convention`
    `jvm-test-convention`
}

dependencies {
    api(project(":domain"))
    implementation(project(":utils"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.tuprolog.solve.api)
    runtimeOnly(libs.tuprolog.solve.classic)

    testImplementation(testFixtures(project(":test-fixtures-domain")))
    testImplementation(libs.mockk)
    testImplementation(libs.tuprolog.parser.core)
    testImplementation(libs.tuprolog.parser.theory)
}
