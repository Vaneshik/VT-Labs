plugins {
    id("java")
    id("application")
    id("jacoco")
}

val lombokVersion = "1.18.36"
val mockitoVersion = "5.16.0"

group = "ru.itmo.qa.lab2"
version = "0.1.0"

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass = "ru.itmo.qa.lab2.Main"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    implementation("org.projectlombok:lombok:${lombokVersion}")
    implementation("ch.obermuhlner:big-math:2.3.2")
    implementation("org.apache.commons:commons-csv:1.13.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.mockito:mockito-junit-jupiter:${mockitoVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12"
    reportsDirectory = layout.buildDirectory.dir("customJacocoReportDir")
}

tasks.test {
    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file(layout.buildDirectory.file("jacoco/jacoco.exec")))
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = false
        csv.required = false
        html.required = true
        html.outputLocation = file(layout.buildDirectory.dir("reports/coverage"))
    }
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.50".toBigDecimal()
            }
        }
    }
}
