package com.example.memoryplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.Adapters.GamesNameListAdapter
import com.example.memoryplay.modles.DataGamesList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GamesListActivity : AppCompatActivity() {

    private lateinit var adapter: GamesNameListAdapter
    private lateinit var rvGameList: RecyclerView
    private val gameCollectionRef = Firebase.firestore.collection("Games")
    private lateinit var btn_showGameNames: Button
    private lateinit var tv_game_list: TextView


    companion object {
        private const val TAG = "Game List Activiy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)
        supportActionBar?.title = "All Games List"

        btn_showGameNames = findViewById(R.id.btn_showGameNames)
        tv_game_list = findViewById(R.id.tvgameList)
        rvGameList = findViewById(R.id.rv_gameList)
        rvGameList.setHasFixedSize(true)
        rvGameList.layoutManager = LinearLayoutManager(this)


        btn_showGameNames.setOnClickListener {
            retreiveGameNames()
        }
    }

    private fun retreiveGameNames() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = gameCollectionRef.get().await()
            val sb = StringBuilder()
            for (document in querySnapshot.documents) {
                val person = document.toObject<DataGamesList>()
                sb.append(person)
            }
            withContext(Dispatchers.Main) {
                Log.e(TAG, "retreiveGameNames: ${sb}/n")
                tv_game_list.text = sb.toString()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@GamesListActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}