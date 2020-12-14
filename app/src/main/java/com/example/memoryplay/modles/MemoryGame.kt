package com.example.memoryplay.modles

import android.util.Log
import com.example.memoryplay.Utils.DEFAULT_ICONS

class MemoryGame(
    private val boardSize: BoardSize,
    customImages: List<String>?
) {

    val cards: List<MemoryCard>
    var numofPairsfound = 0
    var numOfCardFlip = 0
    private var indexofsingleselectedCard: Int? = null

    companion object {
        val TAG = "check fun"
    }

    init {
        // logic for taking random images
        if (customImages == null) {
            val chooseImage = DEFAULT_ICONS.shuffled().take(boardSize.numOfPairs())
            val randomizedImage = (chooseImage + chooseImage).shuffled()
            cards = randomizedImage.map { MemoryCard(it) }
        } else {
            val randamizedImage = (customImages + customImages).shuffled()
            cards = randamizedImage.map { MemoryCard(it.hashCode(), it) }
        }

    }

    fun flipCard(position: Int): Boolean {
        numOfCardFlip++
        val card = cards[position]
        var matchFound = false
        // we have 3 cases here /
        // 0 card flipped >> restore card + flip over
        // 1 card flipped >> filp over and check for a mathch
        // 2 card flipped >> restore cards and flip over current card

        //case 0 and 2
        if (indexofsingleselectedCard == null) {
            restoreCards()
            indexofsingleselectedCard = position
        } else {
            //for 1 case
            matchFound = checkforTheMatch(indexofsingleselectedCard!!, position)
            indexofsingleselectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return matchFound
    }

    private fun checkforTheMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].identifier != cards[position2].identifier) {
            return false
        } else {
            cards[position1].isMatched = true
            cards[position2].isMatched = true
            numofPairsfound++
            Log.i(TAG, "chekforTheMatch: num of pairs found $numofPairsfound")
        }
        return true

    }

    // This function will make previous card down
    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun wonAGame(): Boolean {
        return numofPairsfound == boardSize.numOfPairs()
    }

    fun isFacedUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumOfMoves(): Int {
        return numOfCardFlip / 2
    }


}