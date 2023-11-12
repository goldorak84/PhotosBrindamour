plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "org.brindamour"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.brindamour.Main")
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation("org.apache.poi:poi:5.2.2")
    implementation("org.apache.poi:poi-ooxml:5.2.2")

    implementation("net.sourceforge.tess4j:tess4j:3.5.3")
    implementation("org.apache.commons:commons-imaging:1.0-alpha3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}