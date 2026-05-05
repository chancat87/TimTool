plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.ksp.symbol.processing)
    implementation(libs.kotlinpoet.ksp)
}
