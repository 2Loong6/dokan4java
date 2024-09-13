plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(libs.bundles.jna)

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            groupId = "2Loong6"
            artifactId = "dokan-core"
            version = "1.0-SNAPSHOT"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}