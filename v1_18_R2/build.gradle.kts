group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":shared"))
    compileOnly("org.spigotmc:spigot:1.18.2-R0.1-SNAPSHOT")
    compileOnly("net.coreprotect:coreprotect:21.3")
}