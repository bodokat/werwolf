import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val serializationVersion = "1.3.2"
val ktor_version = "2.0.2"
val logbackVersion = "1.2.10"
val kotlinWrappersVersion = "1.0.0-pre.342"

fun kotlinw(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"

plugins {
    kotlin("multiplatform") version "1.7.0"
    application //to run JVM part
    kotlin("plugin.serialization") version "1.7.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser {
            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("io.ktor:ktor-server-cors:$ktor_version")
                implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-cbor:$ktor_version")
                implementation("io.ktor:ktor-server-html-builder:$ktor_version")
                implementation("io.ktor:ktor-server-websockets:$ktor_version")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project.dependencies.enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.2")

                implementation(kotlinw("mui"))
                implementation(kotlinw("mui-icons"))
                implementation(kotlinw("react"))
                implementation(kotlinw("react-dom"))
                implementation(kotlinw("react-redux"))
                implementation(kotlinw("react-router-dom"))

                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-js:$ktor_version")
                implementation("io.ktor:ktor-client-json:$ktor_version")
                implementation("io.ktor:ktor-client-serialization:$ktor_version")


                implementation(npm("@emotion/react", "11.8.2"))
                implementation(npm("@emotion/styled", "11.8.1"))
            }
        }
    }
}

application {
    mainClass.set("MainKt")
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        // Include js artifacts
        val taskName = if (project.hasProperty("isProduction")
            || project.gradle.startParameter.taskNames.contains("installDist")
        ) {
            "jsBrowserProductionWebpack"
        } else {
            "jsBrowserDevelopmentWebpack"
        }
        val webpackTask = getByName<KotlinWebpack>(taskName)
        dependsOn(taskName) // make sure JS gets compiled first
        from(File(webpackTask.destinationDirectory, webpackTask.outputFileName)) // bring output file along into the JAR

        // Create "fat jar"
        dependsOn.addAll(listOf("compileJava", "compileKotlinJvm","compileKotlinJs","jsBrowserProductionWebpack", "processResources")) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    assemble {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}

tasks.getByName<Jar>("jvmJar") {
    val taskName = if (project.hasProperty("isProduction")
    ) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(webpackTask.destinationDirectory) // bring output file along into the JAR
}


tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {

        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xcontext-receivers")
        }
    }
}


// Alias "installDist" as "stage" (for cloud providers)
tasks.create("stage") {
    dependsOn(tasks.getByName("installDist"))
}

tasks.getByName<JavaExec>("run") {
    classpath(tasks.getByName<Jar>("jvmJar")) // so that the JS artifacts generated by `jvmJar` can be found and served
}
dependencies {
    implementation(kotlin("stdlib-js"))
}