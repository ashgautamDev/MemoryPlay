package com.example.memoryplay.modles


data class MemoryCard(
    val identifier: Int,
    var isFaceUp : Boolean = false,
    var isMatched: Boolean = false
)