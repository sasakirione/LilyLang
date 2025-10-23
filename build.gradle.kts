plugins {
    kotlin("jvm") version "2.2.21"
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

tasks.register<JavaExec>("run") {
    group = "application"
    mainClass.set("MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("test_function.lily")
}
