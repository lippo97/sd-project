import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension

plugins {
    id("com.vanniktech.dependency.graph.generator") version "0.6.0"
}

allprojects {
    version = 0.1
    group = "it.unibo.lpaas"
    repositories {
        mavenCentral()
    }
}

rootProject.configure<DependencyGraphGeneratorExtension> {
    generators.create("internalModules") {
        includeProject = { true }
        children = { false }
    }
}
