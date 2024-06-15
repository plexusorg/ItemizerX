import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "dev.plex"
version = "2.3"

allprojects {
    repositories {
        mavenCentral()

        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            url = uri("https://maven.playpro.com/")
            content {
                includeGroup("net.coreprotect")
            }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.github.goooler.shadow")
    apply(plugin = "io.papermc.paperweight.userdev")

    dependencies {
        if (project.name != "shared") {
            implementation(project(":shared"))
        }
    }

    tasks {
        assemble {
            dependsOn("reobfJar")
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

bukkit {
    name = "ItemizerX"
    version = rootProject.version.toString()
    description = "A new way to edit your items"
    authors = listOf("Focusvity", "Telesphoreo")
    main = "dev.plex.itemizerx.ItemizerX"
    apiVersion = "1.19"
    foliaSupported = true
    softDepend = listOf("CoreProtect")
    commands {
        register("itemizer") {
            description = "The main command for ItemizerX"
            aliases = listOf("ii", "it")
            usage = "/<command>"
        }
    }
}

// Adapted from PlotSquared
val supportedVersions =
        listOf("1.20.2", "1.20.4", "1.20.6", "1.21")
tasks {
    supportedVersions.forEach {
        register<RunServer>("runServer-$it") {
            minecraftVersion(it)
            pluginJars(layout.buildDirectory.file("libs/ItemizerX-${rootProject.version}.jar"))
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run paper"
            runDirectory.set(file("run-$it"))
        }
    }

    assemble {
        dependsOn("shadowJar")
        dependsOn("reobfJar")
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    shadowJar {
        relocate("org.bstats", "dev.plex.metrics")
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("net.coreprotect:coreprotect:22.4")
    implementation("org.bstats:bstats-base:3.0.2")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation(project(path = ":shared", configuration = "shadow"))
    implementation(project(path = ":v1_21_R1", configuration = "shadow"))
    implementation(project(path = ":v1_20_R4", configuration = "shadow"))
    implementation(project(path = ":v1_20_R3", configuration = "shadow"))
    implementation(project(path = ":v1_20_R2", configuration = "shadow"))
}