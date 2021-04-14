@file:Suppress("UnstableApiUsage")

rootProject.name = "backend"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.4.32")
            version("kotlinx", "1.4.3")
            version("micronaut", "2.4.2")
            version("protoc", "3.15.7")
            version("gen-grpc-java", "1.36.1")
            version("gen-grpc-kotlin", "1.0.0")
            version("jjwt", "0.11.2")
            version("mockk", "1.11.0")
            version("turbine", "0.4.1")

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

            alias("javax-annotation").to(
                "javax.annotation",
                "javax.annotation-api"
            ).withoutVersion()

            alias("jjwt-api").to("io.jsonwebtoken", "jjwt-api")
                .versionRef("jjwt")
            alias("jjwt-impl").to("io.jsonwebtoken", "jjwt-impl")
                .versionRef("jjwt")
            alias("jjwt-jackson").to("io.jsonwebtoken", "jjwt-jackson")
                .versionRef("jjwt")

            alias("logback").to("ch.qos.logback", "logback-classic")
                .withoutVersion()

            alias("mockk").to("io.mockk", "mockk").versionRef("mockk")

            alias("turbine").to("app.cash.turbine", "turbine")
                .versionRef("turbine")
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
