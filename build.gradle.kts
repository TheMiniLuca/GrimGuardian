plugins {
    `java-library`
    id("maven-publish")
}

group = "com.gmail.theminiluca.grim.guardian"
version = "1.1.0-SNAPSHOT"
description = "GrimGuardian"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}



// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.