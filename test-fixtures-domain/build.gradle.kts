plugins {
    id("kotlin-common-convention")
    id("java-test-fixtures")
}

dependencies {
    testFixturesImplementation(project(":domain"))
    testFixturesImplementation(libs.tuprolog.parser.core)
    testFixturesImplementation(libs.tuprolog.parser.theory)
}
