buildscript {
    val kotlinVersion = "1.8.20"

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
    signing
    java
}

// a small hack: the variable must be named like the property
val version: String by project
// we create an alias here...
val versionProperty = version
// do the same for group
val group: String by project
val groupEnv: String? = System.getenv("MAVEN_GROUP_ID")
val groupProperty = groupEnv ?: group

listOf(
    "antlr-kotlin-runtime-js",
    "antlr-kotlin-runtime-jvm",
    "antlr-kotlin-runtime-linux",
    "antlr-kotlin-runtime-windows",
).forEach { javadocArtifactId ->
    val javadocTask: Jar by tasks.register(javadocArtifactId, Jar::class) {
        archiveBaseName.set(javadocArtifactId)
        archiveClassifier.set("javadoc")
        tasks["javadoc"].dependsOn(this)
    }

    publishing {
        publications {
            register<MavenPublication>(javadocArtifactId) {
                groupId = groupProperty
                artifactId = javadocArtifactId
                version = versionProperty
                artifact(javadocTask)
            }


        }
    }
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "java")

    // ... because `version` is another var here.
    // when version is hardcoded here, it can not be overwritten here.
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

    signing {
        Triple(
            System.getenv("GPG_SIGNING_KEY_ID"),
            System.getenv("GPG_SIGNING_KEY"),
            System.getenv("GPG_SIGNING_PASSWORD"),
        ).let { (id, key, passwd) ->
            if (id != null && key != null && passwd != null) {
                useInMemoryPgpKeys(id, key, passwd)
                sign(publishing.publications)
            } else if (id != null || key != null || passwd != null) {
                throw GradleException("missing sign id, key or passwd!")
            }
        }
    }

    publishing {
        publications {
            publications.configureEach {
                if (this is MavenPublication) {
                    pom {
                        name.set("antlr-kotlin")
                        description.set("Support for Kotlin as a target for ANTLR")
                        url.set("https://github.com/Strumenta/antlr-kotlin")
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
    gradleVersion = "7.6.4"
    distributionType = Wrapper.DistributionType.ALL
}
