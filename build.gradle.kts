import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources

allprojects {
    group = "su.nightexpress.dungeonarena"
    version = "8.5.1"

    repositories {
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/")
        maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven(url = "https://repo.nightexpressdev.com/releases")
        maven(url = "https://jitpack.io")
        maven(url = "https://mvn.lumine.io/repository/maven-public/")
        maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven(url = "https://repo.codemc.io/repository/maven-public/")
        maven(url = "https://repo.dmulloy2.net/repository/public/")
        maven(url = "https://repo.essentialsx.net/releases/")
        maven(url = "https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven(url = "https://repo.auroramc.gg/releases")
        maven(url = "https://nexus.neetgames.com/repository/maven-public")
    }
}

subprojects {
    apply(plugin = "java-library")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"
    }
}
