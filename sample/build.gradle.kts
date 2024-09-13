plugins {
    java
}

dependencies {
    implementation(project(":dokan-core"))

    implementation(libs.isofilereader)
    testImplementation(libs.bundles.test)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}