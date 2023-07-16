plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.5.5" apply false
    id("xyz.jpenilla.run-paper") version "2.1.0" apply false
}

group = "dev.plex"
version = "2.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "io.papermc.paperweight.userdev")
    apply(plugin = "xyz.jpenilla.run-paper")

    repositories {
        mavenCentral()

        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            url = uri("https://maven.playpro.com/")
        }
    }

    dependencies {
        if (project.name != "shared") {
            implementation(project(":shared"))
        }

        compileOnly("net.coreprotect:coreprotect:21.3")
    }
    tasks {
        assemble {
            dependsOn("reobfJar")
        }
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("ItemizerX")
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    assemble {
        dependsOn("shadowJar")
    }

    jar {
        enabled = false
    }
}

dependencies {
    implementation(project(path = ":core", configuration = "shadow"))
    implementation(project(path = ":shared", configuration = "shadow"))
    implementation(project(path = ":v1_20_R1", configuration = "shadow"))
    implementation(project(path = ":v1_19_R3", configuration = "shadow"))
    implementation(project(path = ":v1_19_R2", configuration = "shadow"))
    implementation(project(path = ":v1_19_R1", configuration = "shadow"))
    implementation(project(path = ":v1_18_R2", configuration = "shadow"))
    implementation(project(path = ":v1_18_R1", configuration = "shadow"))
    implementation(project(path = ":v1_17_R1", configuration = "shadow"))
}