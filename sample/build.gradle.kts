plugins {
    java
}

dependencies {
    implementation(project(":dokan-core"))

    testImplementation(libs.bundles.test)
}
