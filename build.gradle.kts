plugins {
    id("java")
    id("io.freefair.lombok") version "9.8.2"
}

repositories {
    mavenCentral()
}

allprojects {
    apply {
        plugin("java")
        plugin("io.freefair.lombok")
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:26.1.0")
    }
}
