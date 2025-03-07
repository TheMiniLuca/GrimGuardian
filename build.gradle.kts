import xyz.jpenilla.resourcefactory.bukkit.bukkitPluginYaml
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("xyz.jpenilla.resource-factory") version "1.2.0"
    id("com.gradleup.shadow") version "8.3.5"
}
val main = "GrimGuardian"
val minecraftVersion = "1.21.4"
group = "com.gmail.theminiluca.grim.guardian"
version = "1.1.0-SNAPSHOT"
description = "GrimGuardian"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}


repositories {
    mavenLocal()
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    paperweightDevelopmentBundle("build/bundle/jar.jar")
    implementation(project(":main"))

    runtimeOnly(project(":v1_21_R1", configuration = "reobf"))
    runtimeOnly(project(":v1_21_R3", configuration = "reobf"))
}


tasks.jar {
    manifest.attributes(
        "paperweight-mappings-namespace" to "mojang",
    )
}


val bukkitPluginYaml = bukkitPluginYaml {
    main = "${group}.${this@Build_gradle.main}"
    version = project.version.toString()
    description = project.description
    val split = minecraftVersion.split(".")
    apiVersion = split[0] + "." + split[1]
    depend = listOf("packetevents", "GrimAC")
}

sourceSets.main {
    resourceFactory {
        factory(bukkitPluginYaml.resourceFactory())
    }
}


tasks.runServer {
    minecraftVersion("1.21.4")
}

tasks.register("run1_17_1", RunServer::class) {
    minecraftVersion("1.17.1")
    pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
    runDirectory = layout.projectDirectory.dir("run1_17_1")
    systemProperties["Paper.IgnoreJavaVersion"] = true
}

tasks.register("run1_19_4", RunServer::class) {
    minecraftVersion("1.19.4")
    pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
    runDirectory = layout.projectDirectory.dir("run1_19_4")
    systemProperties["Paper.IgnoreJavaVersion"] = true
}


// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.