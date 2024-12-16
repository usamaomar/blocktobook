
val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val kmongoVersion: String by project
val koinVersion: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
//    id ("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"

//    id("com.google.gms.google-services")
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

//tasks.create("stage"){
//    dependsOn("installDist")
//}
tasks{
    create("stage").dependsOn("installDist")
}

repositories {
    mavenCentral()
    maven {url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")}
}

dependencies {

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    // Content Negotiation
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")

    // Sessions
    implementation("io.ktor:ktor-server-sessions-jvm")

    //Auth
    implementation("io.ktor:ktor-server-auth-jvm")

    // KMongo
    implementation("org.litote.kmongo:kmongo-async:$kmongoVersion")
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:$kmongoVersion")

    //Koin
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Google Client API Library
    implementation("com.google.api-client:google-api-client:1.34.0")

    //firebase auth
    implementation("com.google.firebase:firebase-admin:9.0.0")

//    implementation("com.google.firebase:firebase-auth:22.0.0")

    // Google Play services
//    implementation("com.google.android.gms:play-services-auth:20.5.0")

    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-status-pages:2.3.4")
    implementation("ch.qos.logback:logback-classic:1.2.10")

//    implementation("io.ktor:ktor-server-auth:2.3.4")
//    implementation("io.ktor:ktor-server-auth-jwt:2.3.4")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.7.0") // Explicit version, if needed
    implementation("com.auth0:java-jwt:4.2.0")
    ///
    implementation("io.ktor:ktor-server-mustache:2.3.1")
    implementation("io.ktor:ktor-serialization-jackson:2.3.1")
    implementation("io.ktor:ktor-server-cors:2.3.1")
    implementation("io.ktor:ktor-server-request-validation:2.3.1")
    implementation("io.ktor:ktor-server-resources:2.3.1")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.3.1")
    implementation("io.ktor:ktor-server-partial-content:2.3.1")
/// firbase login
    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-client-cio:2.0.0")
    implementation("io.ktor:ktor-client-serialization:2.0.0")
    implementation("io.ktor:ktor-client-json:2.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    ///SendGrid
    implementation("com.sendgrid:sendgrid-java:4.9.3") // or the latest version




    // Google API Client Library
//    implementation("com.google.api-client:google-api-client:2.0.0")



}
