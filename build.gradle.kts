plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.plex"
version = "2.0"

subprojects {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "java")
    java.sourceCompatibility = JavaVersion.VERSION_17

    repositories {
        mavenCentral()

        maven {
            url = uri("https://repo.codemc.org/repository/nms/")
        }

        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            url = uri("https://maven.playpro.com/")
        }
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("ItemizerX")
        archiveClassifier.set("")
        archiveVersion.set("")
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