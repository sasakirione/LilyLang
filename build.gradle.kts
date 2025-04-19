plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.sasakirione"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}