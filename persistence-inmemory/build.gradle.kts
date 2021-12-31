plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":domain"))
    implementation(project(":core"))
}
