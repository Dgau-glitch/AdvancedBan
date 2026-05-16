import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.6"
}

dependencies {
    compileOnly("dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    implementation("org.bstats:bstats-bukkit:2.2.1")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation(project(":core"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
