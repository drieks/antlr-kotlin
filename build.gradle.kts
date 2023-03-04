buildscript {
    val kotlinVersion = "1.8.10"

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    `maven-publish`
    java
}

// a small hack: the variable must be named like the property
// jitpack will pass -Pversion=..., so `val version` is required here.
val version: String by project
// we create an alias here...
val versionProperty = version
// do the same for group
val group: String by project
val groupProperty = if (group.endsWith(".antlr-kotlin")) {
    group
} else {
    // just another jitpack hack
    "$group.antlr-kotlin"
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")

    // ... because `version` is another var here.
    // when version is hardcoded here, jitpack can not overwrite it.
    // the default version can now be changed in gradle.properties
    version = versionProperty
    group = groupProperty

    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.compilerArgs.add("-Xlint:all")
        options.isDeprecation = true
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    publishing {
        publications {
            publications.configureEach {
                if (this is MavenPublication) {
                    pom {
                        name.set("antlr-kotlin")
                        description.set("Support for Kotlin as a target for ANTLR")
                        url.set("http://www.example.com/library")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("ftomassetti")
                                name.set("Federico Tomassetti")
                                email.set("federico@strumenta.com")
                            }
                            developer {
                                id.set("drieks")
                                name.set("Dennis Rieks")
                                email.set("dennisrieks@googlemail.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:git@github.com:Strumenta/antlr-kotlin.git")
                            developerConnection.set("scm:git:git@github.com:Strumenta/antlr-kotlin.git")
                            url.set("https://github.com/Strumenta/antlr-kotlin")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "OSSRH"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.0.2"
    distributionType = Wrapper.DistributionType.ALL
}
