plugins {
    id("kotlin-common-convention")
    id("java-test-fixtures")
}

dependencies {
    testFixturesImplementation(project(":domain"))
    testFixturesImplementation("it.unibo.tuprolog", "parser-core", "0.20.4")
    testFixturesImplementation("it.unibo.tuprolog", "parser-theory", "0.20.4")
}
