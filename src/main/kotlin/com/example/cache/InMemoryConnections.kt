package com.example.cache

import io.ktor.websocket.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashSet


class Connection(val session: DefaultWebSocketSession) {
    companion object {
        var lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}

object InMemoryConnections {
    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
}