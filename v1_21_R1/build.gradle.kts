plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}
dependencies {
    implementation(project(":main"))
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile>().configureEach {
    // Override release for newer MC
    options.release = 21
}