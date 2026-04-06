plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

dependencies {
    implementation(project(":API"))

    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    compileOnly("su.nightexpress.nightcore:main:2.13.3")
    compileOnly("org.jetbrains:annotations:24.0.0")
}
