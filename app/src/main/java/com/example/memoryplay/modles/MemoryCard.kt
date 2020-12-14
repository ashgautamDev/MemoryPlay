package com.example.memoryplay.modles


data class MemoryCard(
    val identifier: Int,
    val imageUrls: String? = null,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)