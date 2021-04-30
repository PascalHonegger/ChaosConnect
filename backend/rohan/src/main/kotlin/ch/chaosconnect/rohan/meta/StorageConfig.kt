package ch.chaosconnect.rohan.meta

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("storage")
class StorageConfig {
    var path: String? = null
}
