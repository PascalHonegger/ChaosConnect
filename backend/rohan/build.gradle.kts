import com.google.protobuf.gradle.*

plugins {
    id("org.jetbrains.kotlin.jvm") version Versions.kotlinVersion
    id("org.jetbrains.kotlin.kapt") version Versions.kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version Versions.kotlinVersion
    id("com.github.johnrengelman.shadow") version Versions.shadowVersion
    id("io.micronaut.application") version Versions.micronautGradlePluginVersion
    id("com.google.protobuf") version Versions.protobufGradlePluginVersion
}

version = About.version
group = About.group

repositories {
    mavenCentral()
}

micronaut {
    version(Versions.micronautVersion)
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("ch.chaosconnect.rohan.*")
    }
}

dependencies {
    implementation("io.micronaut:micronaut-validation")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.grpc:micronaut-grpc-runtime")
    implementation("javax.annotation:javax.annotation-api")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxVersion}")
    implementation("io.grpc:grpc-kotlin-stub:${Versions.protocKotlinVersion}")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("io.mockk:mockk:${Versions.mockkVersion}")
}

application {
    mainClass.set("ch.chaosconnect.rohan.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion(Versions.jvmTargetVersion)
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = Versions.jvmTargetVersion
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = Versions.jvmTargetVersion
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
        artifact = "com.google.protobuf:protoc:${Versions.protocVersion}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.protocJavaVersion}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${Versions.protocKotlinVersion}:jdk7@jar"
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