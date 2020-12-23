package com.example.memoryplay.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.R
import com.example.memoryplay.modles.DataGamesList

class GamesNameListAdapter(
    private val context: Context,
    private val gamesList: List<DataGamesList>
) : RecyclerView.Adapter<GamesNameListAdapter.Viewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_game_list, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val currentName = gamesList[position]
        holder.tv_gameName.setText(currentName.toString())
    }

    override fun getItemCount() = gamesList.size

    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_gameName = itemView.findViewById<TextView>(R.id.tv_gameNames)
    }
}