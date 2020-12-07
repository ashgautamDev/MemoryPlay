package com.example.memoryplay.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryplay.R
import kotlin.math.min

class MemoryBoardAdapter(private val context: Context, private val numPiece: Int) :
    RecyclerView.Adapter<MemoryBoardAdapter.BoardViewHolder>() {

    companion object {
        private const val MARGIN_SIZE = 10
private val TAG = "MemoryBoardAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val cardwidth = parent.width / 2 -(MARGIN_SIZE*2)
        val cardheight = parent.height / 4-(MARGIN_SIZE*2)
        val cardsidelength = min(cardwidth, cardheight)

        val view = LayoutInflater.from(context).inflate(R.layout.item_piece, parent, false)
        val layoutParameters = view.findViewById<CardView>(R.id.DummyCardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParameters.width=cardsidelength
        layoutParameters.height=cardsidelength
        layoutParameters.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)


        return BoardViewHolder(view)

    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = numPiece

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageButton= itemView.findViewById<ImageButton>(R.id.imageButton)
        fun bind(position: Int) {
            imageButton.setOnClickListener { Log.i(TAG, "bind: the position is $position ") }


        }

    }
}

