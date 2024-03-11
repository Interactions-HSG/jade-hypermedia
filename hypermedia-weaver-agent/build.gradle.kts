/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.8.3/userguide/building_java_projects.html
 */

plugins {
  // Apply the java-library plugin for API and implementation separation.
  `java-library`
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
  // Use JCenter for resolving dependencies.
  jcenter()
}

dependencies {
  // Use JUnit test framework.
  // testImplementation("junit:junit:4.13")

  // These dependencies are used internally, and not exposed to consumers on their own compile classpath.
  implementation(files("src/main/resources/jade-4.5.0.jar"))
  implementation("org.eclipse.jetty.aggregate:jetty-all:9.0.0.RC2")
  implementation("org.eclipse.rdf4j:rdf4j-runtime:3.6.1")
  implementation("ch.unisg.ics.interactions.hmas:hmas-java:1.0-SNAPSHOT")
  implementation("ch.unisg.ics.interactions.hmas:interaction:1.0-SNAPSHOT")
  implementation("ch.unisg.ics.interactions.hmas:core:1.0-SNAPSHOT")
}

tasks {
  shadowJar {
    archiveBaseName.set("org_hyperagents_jade_HypermediaWeaverAgent")
    archiveClassifier.set("")
    archiveVersion.set("")

    mergeServiceFiles()
  }

  task<JavaExec>("runMain") {
    mainClass = "jade.Boot"
    args = listOf("-conf", "main.properties", "-gui", "hwa:org.hyperagents.jade.HypermediaWeaverAgent")
    classpath = sourceSets["main"].runtimeClasspath
  }

  task<JavaExec>("runLocal") {
    mainClass = "jade.Boot"
    args = listOf("-container")
    classpath = files("src/main/resources/jade-4.5.0.jar")
  }

  task<JavaExec>("runRemoteHWA") {
    val hwa = "hwa-" + System.currentTimeMillis() + ":org.hyperagents.jade.HypermediaWeaverAgent";

    mainClass = "jade.Boot"
    args = listOf("-container", "-conf", "peripheral.properties", hwa)
    classpath = sourceSets["main"].runtimeClasspath
  }

  task<JavaExec>("runRemote") {
    mainClass = "jade.Boot"
    args = listOf("-container", "-conf", "peripheral.properties")
    classpath = files("src/main/resources/jade-4.5.0.jar")
  }
}
