apply(plugin = "io.papermc.paperweight.userdev")

dependencies {
    implementation(project(":v1_20_R1"))
    implementation(project(":v1_19_R3"))
    implementation(project(":v1_19_R2"))
    implementation(project(":v1_19_R1"))
    implementation(project(":v1_18_R2"))
    implementation(project(":v1_18_R1"))
    implementation(project(":v1_17_R1"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}