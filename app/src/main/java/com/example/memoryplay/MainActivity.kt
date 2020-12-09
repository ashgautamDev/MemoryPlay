package com.example.memoryplay

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.Adapters.MemoryBoardAdapter
import com.example.memoryplay.modles.BoardSize
import com.example.memoryplay.modles.MemoryGame
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var clayout: ConstraintLayout
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvMoves: TextView
    private lateinit var tvPairs: TextView

    private var boardSize: BoardSize = BoardSize.HARD

    companion object {
        val TAG = "main activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clayout = findViewById(R.id.clayout)
        rvBoard = findViewById(R.id.rvBoard)
        tvMoves = findViewById(R.id.tvMoves)
        tvPairs = findViewById(R.id.tvPairs)

        initRecylerview()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.miain_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {


            R.id.refreshButton -> {
                if (memoryGame.getNumOfMoves() > 0 && !memoryGame.wonAGame()) {
                    showAlertDialog(" You Lose Current Game", null, View.OnClickListener {
                        initRecylerview()
                    })
                } else {
                    initRecylerview()
                }
                return true
            }

            R.id.NewLevelButton -> {
                showSizeDaialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSizeDaialog() {
        val boardSizeView =
            LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null, false)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroupSize)
        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.btn_Easy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.btn_medium)
            BoardSize.HARD -> radioGroupSize.check(R.id.btn_hard)
        }
        showAlertDialog("Choose New Level", boardSizeView, View.OnClickListener {
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.btn_Easy -> BoardSize.EASY
                R.id.btn_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }

            initRecylerview()
        })
    }

    private fun showAlertDialog(
        title: String,
        view: View?,
        positiveButtonClickListner: View.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancle ", null)
            .setPositiveButton("Ok") { _, _ ->
                positiveButtonClickListner.onClick(null)
            }.show()

    }

    private fun initRecylerview() {
        when (boardSize) {
            BoardSize.EASY -> {
                tvMoves.text = "Easy :4 x 2"
                tvPairs.text = "Pairs 0/4 "
            }
            BoardSize.MEDIUM -> {
                tvMoves.text = "Medium :6 x 3"
                tvPairs.text = "Pairs 0/9 "
            }
            BoardSize.HARD -> {
                tvMoves.text = "Hard :6 x 3"
                tvPairs.text = "Pairs 0/12 "
            }
        }

        tvPairs.setTextColor(ContextCompat.getColor(this, R.color.progress_start))
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
        if (memoryGame.wonAGame()) {
            Toast.makeText(this, "You Won !!", Toast.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isFacedUp(position)) {
            Toast.makeText(this, "INVALID MOVE", Toast.LENGTH_SHORT).show()
            return
        }

        if (memoryGame.flipCard(position)) {
            val color = ArgbEvaluator().evaluate(
                memoryGame.numofPairsfound.toFloat() / boardSize.numOfPairs(),
                ContextCompat.getColor(this, R.color.progress_start),
                ContextCompat.getColor(this, R.color.progress_end)
            ) as Int

            tvPairs.setTextColor(color)
            tvPairs.text = "Pairs : ${memoryGame.numofPairsfound} / ${boardSize.numOfPairs()}"

            if (memoryGame.wonAGame()) {
                Snackbar.make(clayout, "YOU WON ! Congratulation", Snackbar.LENGTH_LONG).show()
            }
        }


        tvMoves.text = "Moves : ${memoryGame.getNumOfMoves()}"
        adapter.notifyDataSetChanged()
    }


}