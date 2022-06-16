package com.example.cache

import kotlinx.serialization.Serializable

object InMemoryModel {
    val X = arrayListOf<Int>(0) // arrayListOf(1, 2, 3, 4, 5, 25)
    val Y = arrayListOf<Int>(0) // arrayListOf(1, 2, 4, 8, 16, 25)
    val Z = arrayListOf<Int>(0) // arrayListOf(1, 2, 4, 8, 16, 25)
}

fun InMemoryModel.toRemote(): ModelRemote {
    return ModelRemote(X, Y, Z)
}

@Serializable
data class ModelRemote(
    val x: List<Int> = emptyList(),
    val y: List<Int> = emptyList(),
    val z: List<Int> = emptyList()
)

@Serializable
data class StopRemote(
    val type: Int = 1
)

