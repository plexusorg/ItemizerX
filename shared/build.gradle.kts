plugins {
    id("java")
}

group = rootProject.group
version = rootProject.version

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("net.coreprotect:coreprotect:21.3")
}