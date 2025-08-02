import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.runpaper.task.RunServer

plugins {
  `my-conventions`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.18" apply false
  id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer task for testing
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0" // Generates plugin.yml based on the Gradle config
  id("com.gradleup.shadow") version "8.3.5"
}

//java.disableAutoTargetJvm() // Allow consuming JVM 21 projects (i.e. paper_1_21_4) even though our release is 17

val minecraftVersion = "1.21.4"
group = "com.gmail.theminiluca.grim.guardian"
version = "1.1.0-SNAPSHOT"
description = "GrimGuardian"

repositories {
  mavenLocal()
  mavenCentral()
  gradlePluginPortal()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://repo.codemc.io/repository/maven-releases/")
  maven("https://jitpack.io")
  maven("https://repo.grim.ac/snapshots") { // Grim API
    content {
      includeGroup("ac.grim.grimac")
      includeGroup("com.github.retrooper")
    }
  }
  maven {
    name = "scarsz"
    url = uri("https://nexus.scarsz.me/content/groups/public/")
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.gmail.theminiluca.grim.guardian"
      artifactId = "grimguardian"
      version = "1.0.0"
      from(components["java"])
    }
  }
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
  compileOnly("com.github.retrooper:packetevents-spigot:2.9.3")
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  compileOnly("org.projectlombok:lombok:1.18.36")
  annotationProcessor("org.projectlombok:lombok:1.18.36")
  testCompileOnly("org.projectlombok:lombok:1.18.36")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
  compileOnly("org.jetbrains:annotations:26.0.2")

  compileOnly("ac.grim.grimac:bukkit:2.3.72-933f6c745")
//  compileOnly("ac.grim.grimac:common:2.3.72-933f6c745")

  implementation("net.objecthunter:exp4j:0.4.8")

  api("github.scarsz:configuralize:1.4.1") {
    exclude(group = "org.yaml", module = "snakeyaml")
  }
  api("ac.grim.grimac:GrimAPI:1.1.0.0")

  implementation(project(":main"))
  runtimeOnly(project(":v1_21_R1"))
  runtimeOnly(project(":v1_21_R4"))
  runtimeOnly(project(":v1_21_R8"))

  // For Paper 1.20.5+, we don't need to use the reobf variant.
  // If you still support spigot, you will need to use the reobf variant,
  // and remove the Mojang-mapped metadata from the manifest below.
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}

tasks.jar {
  manifest.attributes(
    "paperweight-mappings-namespace" to "mojang",
  )
}

tasks.compileJava {
  options.release = 21
}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
val bukkitPluginYaml = bukkitPluginYaml {
  main = "${group}.${"GrimGuardian"}"
  version = project.version.toString()
  description = project.description
  val split = minecraftVersion.split(".")
  commands {
    create("grimguardian") {
      description = "grimguardian의 메인 명령어"
      usage = "/grimguardian"
      permission = "grimguardian.command"
    }
  }
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


tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
  javaLauncher = javaToolchains.launcherFor {
    vendor = JvmVendorSpec.JETBRAINS
    languageVersion = JavaLanguageVersion.of(21)
  }
  jvmArgs("-XX:+AllowEnhancedClassRedefinition -XX:+EnableDynamicAgentLoading")
}

tasks.register("run1", RunServer::class) {
  minecraftVersion("1.21.1")
  pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
  runDirectory = layout.projectDirectory.dir("run1_21_1")
  systemProperties["Paper.IgnoreJavaVersion"] = true
}

tasks.register("run4", RunServer::class) {
  minecraftVersion("1.21.4")
  pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
  runDirectory = layout.projectDirectory.dir("run1_21_4")
  systemProperties["Paper.IgnoreJavaVersion"] = true
}

tasks.register("run8", RunServer::class) {
  minecraftVersion("1.21.8")
  pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
  runDirectory = layout.projectDirectory.dir("run1_21_8")
  systemProperties["Paper.IgnoreJavaVersion"] = true
}
