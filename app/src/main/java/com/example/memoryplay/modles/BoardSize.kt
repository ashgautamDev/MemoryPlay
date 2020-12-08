package com.example.memoryplay.modles

enum class BoardSize(val numOfCards: Int) {
    EASY(8),
    MEDIUM(18),
    HARD(24);

    fun getColums(): Int {
        return when (this) {
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }

    }
    fun getRows() : Int{
        return numOfCards/getColums()
    }
    fun numOfPairs() : Int{
        return numOfCards/2
    }
}