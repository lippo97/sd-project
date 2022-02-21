import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension

plugins {
    id("com.vanniktech.dependency.graph.generator") version "0.7.0"
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
        include = { it.moduleGroup == "it.unibo.lpaas" }
        children = { it.moduleGroup == "it.unibo.lpaas" }
    }
}
