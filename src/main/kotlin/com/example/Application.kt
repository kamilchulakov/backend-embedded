package com.example

import com.example.CodeNames.delayToShow
import com.example.CodeNames.hst
import com.example.CodeNames.prt
import com.example.CodeNames.stop
import com.example.cache.InMemoryConnections
import com.example.cache.InMemoryModel
import com.example.cache.StopRemote
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
import java.io.File

object CodeNames {
    val stop = "STOP"
    val hst = "192.168.43.200"
    val lst = "192.168.43.89"
    const val prt = 9090
    const val delayToShow = 5000L
}

fun main() {
    runBlocking {

        launch(Dispatchers.IO) {
            println("Включаю вебсокет сервер для фронта")
            embeddedServer(Netty, port = 8000, host = hst) {
                configureRouting()
                configureSockets()
            }.start(wait = true)
        }

        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind(InetSocketAddress(hst, prt))

        launch(Dispatchers.IO) {
            while (true) {
                val socket = serverSocket.accept()
                println("Socket accepted: ${socket.remoteAddress}")
                val receiveChannel = socket.openReadChannel()

                while (true) {
                    val answer = receiveChannel.readUTF8Line()
                    if (answer != null) {
//                        println("ANSWER: $answer")
                        if (answer.contains(stop)) {
                            launch {
                                InMemoryConnections.connections
                                    .drop(InMemoryConnections.connections.size-1)
                                    .forEach {
                                        it.session.sendSerializedBase(
                                            InMemoryModel.toRemote(),
                                            KotlinxWebsocketSerializationConverter(
                                                Json
                                            ), Charsets.UTF_8
                                        )
                                        println("SEND TO: ${it.name}")
                                        it.session.sendSerializedBase(
                                            StopRemote(),
                                            KotlinxWebsocketSerializationConverter(
                                                Json
                                            ), Charsets.UTF_8
                                        )
                                        println("SEND TO: ${it.name} STOP")
                                    }
                                File("somefile7.txt").printWriter().use { out ->
                                    for (idx in InMemoryModel.X.indices) {
                                        out.println("${InMemoryModel.X[idx]} ${InMemoryModel.Y[idx]} ${InMemoryModel.Z[idx]}")
                                    }
                                }
                            }.join()

                            InMemoryModel.X.clear()
                            InMemoryModel.Y.clear()
                            InMemoryModel.Z.clear()

                            break

                        } else {
                            val params = answer.split(" ").map { it.toDouble() }
                            InMemoryModel.X.add(params[0])
                            InMemoryModel.Y.add(params[1])
                            InMemoryModel.Z.add(params[2])
                            // если точки приходят часто, то можно юзать делей, но делей гавно.
//                            if (InMemoryModel.X.size % 5000 == 0) launch {
//
//                                InMemoryConnections.connections
//                                    .drop(InMemoryConnections.connections.size - 1)
//                                    .forEach {
//                                        it.session.sendSerializedBase(
//                                            InMemoryModel.toRemote(),
//                                            KotlinxWebsocketSerializationConverter(
//                                                Json
//                                            ), Charsets.UTF_8
//                                        )
//                                    }
//                                delay(delayToShow)
//                            }.join()

                        }
                    } else break
                }
            }
        }
    }
}
