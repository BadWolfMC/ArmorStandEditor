plugins {
    java
}

group = "io.github.rypofalem.armorstandeditor"
// Plugin release version is defined in gradle.properties (independent of Paper API).

base {
    archivesName.set("armorstandeditor")
}

repositories {
    mavenCentral() // JUnit test dependencies
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    // Only used by the command-option unit tests; not packaged with the plugin.
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

// Run focused unit tests as part of the normal GitHub Actions Gradle build.
tasks.test {
    useJUnitPlatform()
}
