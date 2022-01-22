plugins {
    `kotlin-common-convention`
    `jvm-test-convention`
}

dependencies {
    implementation(project(":domain"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("it.unibo.tuprolog", "solve", "0.20.4")
    runtimeOnly("it.unibo.tuprolog", "solve-classic", "0.20.4")

    testImplementation("io.mockk:mockk:1.12.1")
    testImplementation("it.unibo.tuprolog", "parser-core", "0.20.4")
    testImplementation("it.unibo.tuprolog", "parser-theory", "0.20.4")
}
