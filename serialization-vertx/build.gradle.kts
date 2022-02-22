plugins {
    id("kotlin-common-convention")
}

dependencies {
    api(libs.jackson.databind)
    implementation(project(":domain"))
    implementation(project(":serialization"))
    implementation(libs.vertx.core)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.dataformat.xml)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)

    runtimeOnly(libs.jackson.core)
    runtimeOnly(libs.jackson.annotations)
}
