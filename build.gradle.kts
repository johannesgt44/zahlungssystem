plugins {
    id("java")
    id("application")
}

group = "com.acme"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val jacksonVersion = "2.17.2"
val camundaVersion = "8.9.0"
val rabbitmqVersion = "5.22.0"

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.rabbitmq:amqp-client:$rabbitmqVersion")
    implementation("io.camunda:camunda-client-java:$camundaVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "com.acme.zahlung.ZahlungsServiceWorker"
}

tasks.register<JavaExec>("runZahlungService") {
    group = "application"
    description = "Startet den RabbitMQ-Zahlungsdienst."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.acme.zahlung.ZahlungsServiceWorker"
}

tasks.register<JavaExec>("runZahlungsauftragCamundaWorker") {
    group = "application"
    description = "Startet den Camunda Worker, der Zahlungsauftraege in RabbitMQ legt."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.acme.zahlung.ZahlungsauftragCamundaWorker"
}

tasks.test {
    useJUnitPlatform()
}
