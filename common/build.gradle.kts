plugins {
    alias(libs.plugins.multimod)
}

multimod.common()

dependencies {
    compileOnly(libs.bluemap.api)
    testImplementation(libs.bluemap.api)
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
