package com.example.memoryplay

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.Adapters.MemoryBoardAdapter
import com.example.memoryplay.modles.BoardSize
import com.example.memoryplay.modles.MemoryGame

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvMoves: TextView
    private lateinit var tvPairs: TextView

    private var boardSize: BoardSize = BoardSize.MEDIUM

    companion object {
        val TAG = "main activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        tvMoves = findViewById(R.id.tvMoves)
        tvPairs = findViewById(R.id.tvPairs)

        initRecylerview()

    }

    private fun initRecylerview() {

        memoryGame = MemoryGame(boardSize)
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards,
            object : MemoryBoardAdapter.CardclickListner {
                override fun OncardClickListener(position: Int) {
                    updateOverCardflip(position)
                }
            })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getColums())
    }

    private fun updateOverCardflip(position: Int) {

        //errors handeling
        if ( memoryGame.wonAGame()){
            Toast.makeText(this, "You Won !!", Toast.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isFacedUp(position)){
            Toast.makeText(this, "INVALID MOVE", Toast.LENGTH_SHORT).show()
            return}
        memoryGame.flipCard(position)
        adapter.notifyDataSetChanged()
    }


}