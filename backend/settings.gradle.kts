@file:Suppress("UnstableApiUsage")

rootProject.name = "backend"

enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    val kotlinPluginVersion = "1.5.0"
    val shadowPluginVersion = "7.0.0"
    val micronautPluginVersion = "1.5.0"
    val protobufPluginVersion = "0.8.16"
    plugins {
        kotlin("jvm") version kotlinPluginVersion
        kotlin("kapt") version kotlinPluginVersion
        kotlin("plugin.allopen") version kotlinPluginVersion
        kotlin("plugin.serialization") version kotlinPluginVersion
        id("com.github.johnrengelman.shadow") version shadowPluginVersion
        id("io.micronaut.application") version micronautPluginVersion
        id("com.google.protobuf") version protobufPluginVersion
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.5.0")
            version("kotlinx", "1.5.0")
            version("serialization-json", "1.2.1")
            version("micronaut", "2.5.3")
            version("protoc", "3.16.0")
            version("gen-grpc-java", "1.37.1")
            version("gen-grpc-kotlin", "1.1.0")
            version("jjwt", "0.11.2")
            version("mockk", "1.11.0")
            version("turbine", "0.5.0")
            version("argon2-jvm", "2.10.1")

            alias("kotlin-stdlib").to(
                "org.jetbrains.kotlin",
                "kotlin-stdlib-jdk8"
            ).versionRef("kotlin")
            alias("kotlin-reflect").to("org.jetbrains.kotlin", "kotlin-reflect")
                .versionRef("kotlin")
            bundle("kotlin", listOf("kotlin-stdlib", "kotlin-reflect"))

            alias("kotlinx-core").to(
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-core"
            ).versionRef("kotlinx")
            alias("kotlinx-reactive").to(
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-reactive"
            ).versionRef("kotlinx")
            alias("kotlinx-test").to(
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-test"
            ).versionRef("kotlinx")
            alias("kotlinx-serialization-json").to(
                "org.jetbrains.kotlinx",
                "kotlinx-serialization-json"
            ).versionRef("serialization-json")

            alias("micronaut-runtime").to("io.micronaut", "micronaut-runtime")
                .withoutVersion()
            alias("micronaut-management").to(
                "io.micronaut",
                "micronaut-management"
            ).withoutVersion()
            alias("micronaut-validation").to(
                "io.micronaut",
                "micronaut-validation"
            ).withoutVersion()
            alias("micronaut-http-server-netty").to(
                "io.micronaut",
                "micronaut-http-server-netty"
            ).withoutVersion()
            alias("micronaut-kotlin-runtime").to(
                "io.micronaut.kotlin",
                "micronaut-kotlin-runtime"
            ).withoutVersion()
            alias("micronaut-grpc-runtime").to(
                "io.micronaut.grpc",
                "micronaut-grpc-runtime"
            ).withoutVersion()
            bundle(
                "micronaut-server",
                listOf(
                    "micronaut-runtime",
                    "micronaut-management",
                    "micronaut-validation",
                    "micronaut-http-server-netty",
                    "micronaut-kotlin-runtime",
                    "micronaut-grpc-runtime"
                )
            )

            alias("grpc-kotlin").to("io.grpc", "grpc-kotlin-stub")
                .versionRef("gen-grpc-kotlin")

            alias("micronaut-http-client").to(
                "io.micronaut",
                "micronaut-http-client"
            ).withoutVersion()
            bundle("micronaut-test", listOf("micronaut-http-client"))

            alias("jjwt-api").to("io.jsonwebtoken", "jjwt-api")
                .versionRef("jjwt")
            alias("jjwt-impl").to("io.jsonwebtoken", "jjwt-impl")
                .versionRef("jjwt")
            alias("jjwt-jackson").to("io.jsonwebtoken", "jjwt-jackson")
                .versionRef("jjwt")

            alias("argon2-jvm").to("de.mkammerer", "argon2-jvm")
                .versionRef("argon2-jvm")

            alias("logback").to("ch.qos.logback", "logback-classic")
                .withoutVersion()

            alias("mockk").to("io.mockk", "mockk").versionRef("mockk")

            alias("turbine").to("app.cash.turbine", "turbine")
                .versionRef("turbine")

            alias("junit-params").to(
                "org.junit.jupiter",
                "junit-jupiter-params"
            ).withoutVersion()
        }
        create("targets") {
            version("jvm", "15")
        }
        create("meta") {
            version("main", "0.1")
        }
    }
}

include("joestar", "rohan")
