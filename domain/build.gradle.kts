plugins {
    id("kotlin-common-convention")
}

dependencies {
    implementation(project(":utils"))

    api("it.unibo.tuprolog", "core", "0.20.4")
    implementation("it.unibo.tuprolog", "theory", "0.20.4")
    implementation("it.unibo.tuprolog", "parser-core", "0.20.4")
    implementation("it.unibo.tuprolog", "parser-theory", "0.20.4")
    implementation("it.unibo.tuprolog", "serialize-core", "0.20.4")
    implementation("it.unibo.tuprolog", "serialize-theory", "0.20.4")
}
