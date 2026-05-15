plugins { kotlin("jvm") }

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-api:2.0.13")

    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
