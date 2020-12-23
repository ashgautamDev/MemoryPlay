package com.example.memoryplay

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.Adapters.MemoryBoardAdapter
import com.example.memoryplay.Utils.EXTRA_BOARD_SIZE
import com.example.memoryplay.Utils.EXTRA_GAME_NAME
import com.example.memoryplay.modles.BoardSize
import com.example.memoryplay.modles.MemoryGame
import com.example.memoryplay.modles.UserImageList
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var clayout: CoordinatorLayout
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvMoves: TextView
    private lateinit var tvPairs: TextView
    private lateinit var btn_next: Button



    private var boardSize: BoardSize = BoardSize.EASY
    private val database = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages: List<String>? = null

    companion object {
        private const val TAG = "main activity"
        private const val REQUEST_CODE = 30
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clayout = findViewById(R.id.clayout)
        rvBoard = findViewById(R.id.rvBoard)
        tvMoves = findViewById(R.id.tvMoves)
        tvPairs = findViewById(R.id.tvPairs)
        btn_next = findViewById(R.id.nextButton)


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
                    showAlertDialog(" You Will Lose Current Game", null, View.OnClickListener {
                        initRecylerview()
                    })
                } else {
                    initRecylerview()
                }
                return true
            }

            R.id.mi_NewLevelButton -> {
                showDialogForGameType()
                return true
            }
            R.id.CreateGameButton -> {
                showsSizeForCreatingCustomeGame()
                return true
            }
            R.id.btn_download -> {
                showDownloadDialoge()
                return true
            }
            R.id.btn_gameList -> {
                val intent = Intent(this, GamesListActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && requestCode == Activity.RESULT_OK) {
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if (customGameName == null) {
                Log.e(TAG, "Got null custom game from CreateActivity")
                return
            }
            downloadGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)

    }


    private fun showDownloadDialoge() {
        val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.download_dialoge, null)
        showAlertDialog("Downlod Game ", boardDownloadView, View.OnClickListener {
            val etGameName = boardDownloadView.findViewById<EditText>(R.id.etDownloadgame)
            val GameName = etGameName.text.toString().trim()
            downloadGame(GameName)
        })
    }


    private fun downloadGame(customGameName: String) {
        Log.e(TAG, "downloadGame: downloding is in processing")
        database.collection("Games").document(customGameName).get()
            .addOnSuccessListener { document ->
                val userImageList = document.toObject(UserImageList::class.java)
                if (userImageList?.images == null) {
                    Log.e(TAG, "Invalid custom game data from Firebase")
                    Snackbar.make(
                        clayout,
                        "Sorry no game found this name '$gameName'",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
                val numCards = userImageList.images.size * 2
                boardSize = BoardSize.getCardValues(numCards)
                customGameImages = userImageList.images
                initRecylerview()
                gameName = customGameName

                for (imageurl in userImageList.images) {
                    Picasso.get().load(imageurl).fetch()
                }


            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Exception when retrieving the game ", exception)
            }
    }

    private fun showsSizeForCreatingCustomeGame() {
        val newBoardSizeView =
            LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null, false)
        val radioGroupSize = newBoardSizeView.findViewById<RadioGroup>(R.id.radioGroupSize)
        //Intially checked to easy
        radioGroupSize.check(R.id.btn_Easy)
        showAlertDialog("Choose Size", newBoardSizeView, View.OnClickListener {
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.btn_Easy -> BoardSize.EASY
                R.id.btn_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD

            }
            Log.i(TAG, "showsSizeForCreatingCustomeGame: You Can Launch New Activity Here")
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, REQUEST_CODE)

        })

    }

    private fun showDialogForGameType() {
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

            gameName = null
            customGameImages = null
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
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
        btn_next.isVisible = false
        memoryGame = MemoryGame(boardSize, customGameImages)
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

        //Errors handeling
        if (memoryGame.wonAGame()) {
            Toast.makeText(this, "You Won !!", Toast.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isFacedUp(position)) {
            Toast.makeText(this, "INVALID MOVE", Toast.LENGTH_SHORT).show()
            return
        }

        if (memoryGame.flipCard(position)) {
            val colorPairs = ArgbEvaluator().evaluate(
                memoryGame.numofPairsfound.toFloat() / boardSize.numOfPairs(),
                ContextCompat.getColor(this, R.color.progress_start),
                ContextCompat.getColor(this, R.color.progress_end)
            ) as Int

            tvPairs.setTextColor(colorPairs)
            tvPairs.text = "Pairs : ${memoryGame.numofPairsfound} / ${boardSize.numOfPairs()}"

            if (memoryGame.wonAGame()) {
                Snackbar.make(clayout, "YOU WON ! Congratulation", Snackbar.LENGTH_LONG).show()
                CommonConfetti.rainingConfetti(
                    clayout,
                    intArrayOf(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                ).oneShot()
                btn_next.isVisible = true
                btn_next.setOnClickListener {
                    boardSize = when (boardSize) {
                        BoardSize.EASY -> BoardSize.MEDIUM
                        BoardSize.MEDIUM -> BoardSize.HARD
                        BoardSize.HARD -> BoardSize.HARD
                    }
                    initRecylerview()
                }
            }
        }
        tvMoves.text = "Moves : ${memoryGame.getNumOfMoves()}"
        adapter.notifyDataSetChanged()
    }
}


