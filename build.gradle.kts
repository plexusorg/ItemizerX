import xyz.jpenilla.runpaper.task.RunServer
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
}

group = "dev.plex"
version = "3.0.0"

val paperApiVersion = providers.gradleProperty("paperApiVersion")
    .getOrElse("26.2.build.112-stable")

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
    apply(plugin = "com.gradleup.shadow")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

paper {
    name = "ItemizerX"
    version = rootProject.version.toString()
    description = "A new way to edit your items"
    authors = listOf("Focusvity", "Telesphoreo")
    main = "dev.plex.itemizerx.ItemizerX"
    apiVersion = "26.1"
    foliaSupported = true

    serverDependencies {
        register("CoreProtect") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }

    permissions {
        register("itemizer.use") {
            description = "Allows use of ItemizerX"
        }
        register("itemizer.component") {
            description = "Allows editing item data components"
        }
        register("itemizer.data") {
            description = "Allows editing ItemizerX persistent item data"
        }
        register("itemizer.data.foreign") {
            description = "Allows editing persistent data owned by other namespaces"
        }
        register("itemizer.container") {
            description = "Allows editing bundle and container contents"
        }
        register("itemizer.preset") {
            description = "Allows saving, importing, exporting, and loading item presets"
        }
        register("itemizer.gui") {
            description = "Allows opening the ItemizerX inventory editor"
        }
        register("itemizer.attr.edit") {
            description = "Allows editing existing item attribute modifiers"
        }
    }
}

// Adapted from PlotSquared
// Paper never published a downloadable 26.1 server build; 26.1.1 was the first 26.1.x build.
val supportedVersions = listOf("26.1.1", "26.1.2", "26.2")
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
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "dev.plex.metrics")
    }

    jar {
        archiveClassifier.set("unshaded")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly("net.coreprotect:coreprotect:24.0")
    implementation("org.bstats:bstats-base:3.2.1")
    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation(project(path = ":shared", configuration = "shadow"))
}
