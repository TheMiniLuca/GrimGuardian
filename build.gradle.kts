import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.runpaper.task.RunServer

plugins {
  `my-conventions`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.14" apply false
  id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer task for testing
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0" // Generates plugin.yml based on the Gradle config
  id("com.gradleup.shadow") version "8.3.5"
}

java.disableAutoTargetJvm() // Allow consuming JVM 21 projects (i.e. paper_1_21_4) even though our release is 17

val main = "GrimGuardian"
val minecraftVersion = "1.21.4"
group = "com.gmail.theminiluca.grim.guardian"
version = "1.1.0-SNAPSHOT"
description = "GrimGuardian"

repositories {
  mavenLocal()
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {




  compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
  compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  compileOnly("org.projectlombok:lombok:1.18.36")
  annotationProcessor("org.projectlombok:lombok:1.18.36")
  testCompileOnly("org.projectlombok:lombok:1.18.36")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
  compileOnly("org.jetbrains:annotations:26.0.2")
  compileOnly("ac.grim.grimac:grimac:2.3.67")
  implementation("net.objecthunter:exp4j:0.4.8")

  implementation(project(":main"))

  // Shade the reobf variant
  runtimeOnly(project(":v1_21_R1", configuration = "reobf"))

  // For Paper 1.20.5+, we don't need to use the reobf variant.
  // If you still support spigot, you will need to use the reobf variant,
  // and remove the Mojang-mapped metadata from the manifest below.
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}

//tasks.jar {
//  manifest.attributes(
//    "paperweight-mappings-namespace" to "mojang",
//  )
//}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
val bukkitPluginYaml = bukkitPluginYaml {
  main = "${group}.${this@Build_gradle.main}"
  version = project.version.toString()
  description = project.description
  val split = minecraftVersion.split(".")
  apiVersion = split[0] + "." + split[1]
  depend = listOf("packetevents", "GrimAC")
}

tasks {
  compileJava {
    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
}

tasks.runServer {
  minecraftVersion("1.21.4")
}

tasks.register("run1_21_1", RunServer::class) {
  minecraftVersion("1.21.1")
  pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
  runDirectory = layout.projectDirectory.dir("run1_21_1")
  systemProperties["Paper.IgnoreJavaVersion"] = true
}

tasks.register("run1_21_4", RunServer::class) {
  minecraftVersion("1.21.4")
  pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
  runDirectory = layout.projectDirectory.dir("run1_21_4")
  systemProperties["Paper.IgnoreJavaVersion"] = true
}
