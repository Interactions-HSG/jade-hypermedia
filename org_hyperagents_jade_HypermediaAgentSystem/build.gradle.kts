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
  id("com.github.johnrengelman.shadow") version "5.1.0"
}

repositories {
  // Use JCenter for resolving dependencies.
  jcenter()
}

dependencies {
  // Use JUnit test framework.
  // testImplementation("junit:junit:4.13")

  // This dependency is exported to consumers, that is to say found on their compile classpath.
  // api("org.apache.commons:commons-math3:3.6.1")

  // These dependencies are used internally, and not exposed to consumers on their own compile classpath.
  implementation(files("src/main/resources/jade-4.5.0.jar"))
  implementation("org.eclipse.jetty.aggregate:jetty-all:9.0.0.RC2")
  implementation("org.eclipse.rdf4j:rdf4j-runtime:3.6.1")
}

tasks {
  shadowJar {
    archiveBaseName.set("org_hyperagents_jade_HypermediaAgentSystem")
    archiveClassifier.set("")
    archiveVersion.set("")

    mergeServiceFiles()
  }

  task<JavaExec>("run") {
    main = "jade.Boot"

    args = listOf("-gui", "-jade_core_management_AgentManagementService_agentspath",
      "org_hyperagents_jade_HypermediaAgentSystem/build/libs/",
      "has:org.hyperagents.jade.HypermediaAgentSystem")

    classpath = sourceSets["main"].runtimeClasspath
  }
}
