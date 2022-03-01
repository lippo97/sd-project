plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.vertx.core)
    implementation(libs.bcrypt)
}
