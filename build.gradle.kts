plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
}

// Configure source directories for stubs and tests
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    kotlinOptions.jvmTarget = "17"
}

sourceSets {
    named("main") {
        kotlin.srcDir("android-stubs/src/main/kotlin")
    }
    named("test") {
        kotlin.srcDir("android/app/src/test/java")
    }
}
