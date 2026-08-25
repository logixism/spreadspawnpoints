import org.gradle.api.tasks.SourceTask
import org.gradle.jvm.tasks.Jar

plugins {
    alias(libs.plugins.multimod)
}

multimod.modPublishing {
    modrinth {
        // Fabric API
        requires {
            slug = "P7dR8mSH"
        }
    }
}

multimod.fabric(project(":common"))

dependencies {
    compileOnly(libs.bluemap.api)
}

// MultiMod includes every common source set; keep common tests out of loader artifacts.
val commonTestSources = project(":common").file("src/test").toPath()
tasks.withType<SourceTask>().configureEach {
    exclude { it.file.toPath().startsWith(commonTestSources) }
}
tasks.named<Jar>("sourcesJar") {
    exclude { it.file.toPath().startsWith(commonTestSources) }
}
