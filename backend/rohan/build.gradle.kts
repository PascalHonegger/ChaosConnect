import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.allopen")
    id("com.github.johnrengelman.shadow")
    id("io.micronaut.application")
    id("com.google.protobuf")
}

version = meta.versions.main.get()
group = "ch.chaosconnect"

repositories {
    mavenCentral()
}

micronaut {
    version(libs.versions.micronaut.get())
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("ch.chaosconnect.rohan.*")
    }
}

dependencies {
    implementation(libs.bundles.micronaut.server)
    implementation(libs.javax.annotation)
    implementation(libs.bundles.kotlin)
    implementation(libs.kotlinx.core)
    implementation(libs.kotlinx.reactive)
    implementation(libs.grpc.kotlin)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.logback)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    testImplementation(libs.bundles.micronaut.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.test)
}

application {
    mainClass.set("ch.chaosconnect.rohan.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion(targets.versions.jvm.get())
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = targets.versions.jvm.get()
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = targets.versions.jvm.get()
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
            srcDirs("build/generated/source/proto/main/java")
        }
        proto {
            srcDir("${project.rootDir}/grpc")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protoc.get()}"
    }
    plugins {
        id("grpc") {
            artifact =
                "io.grpc:protoc-gen-grpc-java:${libs.versions.gen.grpc.java.get()}"
        }
        id("grpckt") {
            artifact =
                "io.grpc:protoc-gen-grpc-kotlin:${libs.versions.gen.grpc.kotlin.get()}:jdk7@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
