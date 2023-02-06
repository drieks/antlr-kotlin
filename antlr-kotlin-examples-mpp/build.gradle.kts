// a small hack: the variable must be named like the property
val version: String by project
// we create an alias here...
val versionProperty = version
// do the same for group
val group: String by project
val groupEnv: String? = System.getenv("MAVEN_GROUP_ID")
val groupProperty = groupEnv ?: group

//val antlrVersion = "4.7.1"
val antlrKotlinVersion = versionProperty

buildscript {
    // we have to re-declare this here :-(

    // a small hack: the variable must be named like the property
    val version: String by project
    // we create an alias here...
    val versionProperty = version
    // do the same for group
    val group: String by project
    val groupEnv: String? = System.getenv("MAVEN_GROUP_ID")
    val groupProperty = groupEnv ?: group

    val antlrKotlinVersion = versionProperty

    dependencies {
        // add the plugin to the classpath
        classpath("$groupProperty:antlr-kotlin-gradle-plugin:$antlrKotlinVersion")
    }
}

repositories {
    // used for local development
    mavenLocal()
    // used to download antlr4
    mavenCentral()
}

plugins {
    kotlin("multiplatform") version "1.8.20"
    // do not add the plugin here, it contains only a task
    //id("com.strumenta.antlr-kotlin") version "0.0.5"
}

kotlin {
    jvm()

    sourceSets {
        val commonAntlr by creating {
            dependencies {
                api(kotlin("stdlib-common"))
                // add antlr-kotlin-runtime
                // otherwise, the generated sources will not compile
                api("$groupProperty:antlr-kotlin-runtime:$antlrKotlinVersion")
                // antlr-kotlin-runtime-jvm is automatically added as an jvm dependency by gradle
            }
            // you have to add the generated sources the to the kotlin compiler source directory list
            // this is not required if you use src/commonAntlr/kotlin
            // and want to add the generated sources to version control
            kotlin.srcDir("build/generated-src/commonAntlr/kotlin")
        }

        val commonMain by getting {
            dependsOn(commonAntlr)
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(kotlin("reflect"))
            }
        }

        val jvmTest by getting {
            dependencies {
                api(kotlin("test-junit5"))
            }
        }
    }
}

// in antlr-kotlin-plugin <0.0.5, the configuration was applied by the plugin.
// starting from verison 0.0.5, you have to apply it manually:
tasks.register<com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask>("generateKotlinCommonGrammarSource") {
    // the classpath used to run antlr code generation
    antlrClasspath = configurations.detachedConfiguration(
            // antlr itself
            // antlr is transitive added by antlr-kotlin-target,
            // add another dependency if you want to choose another antlr4 version (not recommended)
            // project.dependencies.create("org.antlr:antlr4:$antlrVersion"),

            // antlr target, required to create kotlin code
            project.dependencies.create("$groupProperty:antlr-kotlin-target:$antlrKotlinVersion")
    )
    maxHeapSize = "64m"
    packageName = "com.strumenta.antlrkotlin.examples"
    arguments = listOf("-no-visitor", "-no-listener")
    source = project.objects
            .sourceDirectorySet("antlr", "antlr")
            .srcDir("src/commonAntlr/antlr").apply {
                include("*.g4")
            }
    // outputDirectory is required, put it into the build directory
    // if you do not want to add the generated sources to version control
    outputDirectory = File("build/generated-src/commonAntlr/kotlin")
    // use this settings if you want to add the generated sources to version control
    // outputDirectory = File("src/commonAntlr/kotlin")
}

// run generate task before build
// not required if you add the generated sources to version control
// you can call the task manually in this case to update the generated sources
tasks.getByName("compileKotlinJvm").dependsOn("generateKotlinCommonGrammarSource")

// to allow -x jsIrBrowserTest -x jsLegacyBrowserTest, see .ci.sh
tasks.register("jsIrBrowserTest")
tasks.register("jsLegacyBrowserTest")
