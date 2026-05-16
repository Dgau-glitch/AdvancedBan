plugins { kotlin("jvm") }

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    compileOnly("net.md-5:bungeecord-api:1.12-SNAPSHOT")
    compileOnly("de.dytanic.cloudnet:cloudnet-driver:3.3.0-RELEASE")
    compileOnly("de.dytanic.cloudnet:cloudnet-bridge:3.3.0-RELEASE")
    compileOnly("de.dytanic.cloudnet:cloudnet-api-bridge:2.1.17")
    compileOnly("de.dytanic.cloudnet:cloudnet-core:2.1.17")
    compileOnly("net.luckperms:api:5.2")
    compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.6-SNAPSHOT")
        implementation(project(":core"))
}
