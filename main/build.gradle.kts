plugins {
  `my-conventions`
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.gmail.theminiluca.grim.guardian"
      artifactId = "grimguardian-main"
      version = "1.0.0"
      from(components["java"])
    }
  }
}
dependencies {

  compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
  compileOnly("org.projectlombok:lombok:1.18.36")
  annotationProcessor("org.projectlombok:lombok:1.18.36")
  testCompileOnly("org.projectlombok:lombok:1.18.36")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
  compileOnly("org.jetbrains:annotations:26.0.2")
}
