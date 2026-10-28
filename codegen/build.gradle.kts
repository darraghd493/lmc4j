plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ast"))

    implementation(platform("org.ow2.asm:asm-bom:9.11.1"))
    implementation("org.ow2.asm:asm")
    implementation("org.ow2.asm:asm-tree")

    testImplementation(project(":parser"))

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}