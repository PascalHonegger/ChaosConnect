package ch.chaosconnect.joestar

import javax.inject.Singleton

@Singleton
class EchoService {
    fun echo(message: String): String {
        return "Joestar said $message"
    }
}