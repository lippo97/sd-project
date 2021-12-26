plugins {
    `kotlin-common-convention`
    `jvm-test-convention`
}

dependencies {
    implementation(project(":domain"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
}
