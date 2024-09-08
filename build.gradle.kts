plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.2"
}

group = "com.gmail.theminiluca.grim.guardian"
version = "1.0-SNAPSHOT"
description = "GrimGuardian"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

// 1)
// For >=1.20.5 when you don't care about supporting spigot
// paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

// 2)
// For 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5
// Configure reobfJar to run when invoking the build task
/*
tasks.assemble {
  dependsOn(tasks.reobfJar)
}
 */
repositories {
    mavenLocal()
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.4.0")
    compileOnly("ac.grim.grimac:grimac:2.3.67")
    // paperweight.foliaDevBundle("1.21-R0.1-SNAPSHOT")
    // paperweight.devBundle("com.example.paperfork", "1.21-R0.1-SNAPSHOT")
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

    // Only relevant when going with option 2 above
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar = layout.buildDirectory.file("C:/Users/themi/Desktop/lastest/plugins/update/${project.name}-${project.version}.jar")
    }
}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.