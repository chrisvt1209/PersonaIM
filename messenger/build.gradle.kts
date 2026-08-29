plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.sirhcvt"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(platform("io.insert-koin:koin-bom:4.2.2"))

    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.websockets)
    implementation(libs.logback.classic)

    implementation("org.flywaydb:flyway-core:10.20.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.20.1")

    implementation("io.insert-koin:koin-ktor")
    implementation("io.insert-koin:koin-logger-slf4j")

    implementation("org.mindrot:jbcrypt:0.4")

    implementation("org.ktorm:ktorm-core:4.1.1")
    implementation("org.ktorm:ktorm-support-postgresql:4.1.1")
    implementation("org.postgresql:postgresql:42.7.7")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
