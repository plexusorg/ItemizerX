val paperApiVersion = providers.gradleProperty("paperApiVersion")
    .getOrElse("26.2.build.112-stable")

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly("net.coreprotect:coreprotect:24.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(25)
}
