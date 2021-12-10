plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.6.0"))
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.19.0")
}
