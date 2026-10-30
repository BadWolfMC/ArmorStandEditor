plugins {
    java
}

group = "io.github.rypofalem.armorstandeditor"
version = "26.3"

base {
    archivesName.set("armorstandeditor")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

val pluginVersion = project.version.toString()

tasks.processResources {
    filteringCharset = "UTF-8"
    inputs.property("pluginVersion", pluginVersion)
    filesMatching("plugin.yml") {
        // Keep the legacy ${project.version} template in plugin.yml unchanged.
        expand("project" to mapOf("version" to pluginVersion))
    }
}
