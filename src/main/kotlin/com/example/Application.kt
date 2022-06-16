package com.example

import com.example.cache.InMemoryConnections
import com.example.cache.InMemoryModel
import com.example.cache.toRemote
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.serialization.kotlinx.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun main() {
    runBlocking {
        println("Введите адрес сервера")
        val hst = "192.168.43.200"
        println("Введите адрес апельсинки")
        val lst = "192.168.43.89"
        println("Введите порт апельсинки")
        val prt = 9090

        launch(Dispatchers.IO) {
            println("Включаю вебсокет сервер для фронта")
            embeddedServer(Netty, port = 8000, host = hst) {
                configureRouting()
                configureSockets()
            }.start(wait = true)
        }

        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind(InetSocketAddress(hst, prt))
        val socket = serverSocket.accept()
        println("Socket accepted: ${socket.remoteAddress}")
        val receiveChannel = socket.openReadChannel()
        launch(Dispatchers.IO) {

            while (true) {
                val answer = receiveChannel.readUTF8Line()
                if (answer != null) {
                    println("ANSWER: $answer")
                    println(InMemoryConnections.connections)
                    val params = answer.split(" ").map { it.toInt() }
                    InMemoryModel.X.add(params[0])
                    InMemoryModel.Y.add(params[1])
                    InMemoryModel.Z.add(params[2])
                    launch {
                        InMemoryConnections.connections
                            .forEach {
                                it.session.sendSerializedBase(
                                    InMemoryModel.toRemote(),
                                    KotlinxWebsocketSerializationConverter(
                                        Json
                                    ), Charsets.UTF_8
                                )
                                println("Send ${it.name}")
                            }
                    }.join()
                }
            }
        }
    }
}
