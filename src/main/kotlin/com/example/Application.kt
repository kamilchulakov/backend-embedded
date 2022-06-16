package com.example

import com.example.cache.InMemoryModel
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import kotlinx.coroutines.runBlocking

fun main() {
    embeddedServer(Netty, port = 8000, host = "0.0.0.0") {
        configureRouting()
        configureSockets()
    }.start(wait = true)
}
