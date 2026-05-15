plugins { kotlin("jvm") }

dependencies {
    compileOnly("dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation(project(":core"))
}
